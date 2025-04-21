package com.example.underskrift_export.services;

import com.example.underskrift_export.mapper.SignDataMapper;

import com.example.underskrift_export.models.SignatureDataUbmEntity;
import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.repositories.SignatureDataRepository;
import com.example.underskrift_export.utils.BatchFileHelper;
import com.example.underskrift_export.utils.SignDataSchemaValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.DeliveryMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Service
@Slf4j
public class SignDataService {

    public static final String SIGNATURE_DATA_UBM_TABLE = "signature_data_ubm";
    public static final String EXPORTED_SIGNATURE_DATA_TABLE = "exported_signature_data";
    public static final int GZIP_TRESHOLD = 100000;
    private final SignatureDataRepository signatureDataRepository;
    private final SignDataMapper signDataMapper;
    private final ObjectMapper objectMapper;
    private final SignDataSchemaValidator signDataSchemaValidator;
    private final JmsTemplate jmsQueueTemplate;
    private final BatchFileHelper batchFileHelper;
    private final ExportedSignatureDataService exportedSignatureDataService;
    private final JdbcTemplate jdbcTemplate;
    private static final int DELETE_BATCH_SIZE = 1000;
    private static final int ARCHIVE_BATCH_SIZE = 1000;

    public SignDataService(SignatureDataRepository signatureDataRepository,
                           SignDataMapper signDataMapper,
                           ObjectMapper objectMapper,
                           SignDataSchemaValidator signDataSchemaValidator,
                           @Qualifier("jmsQueueTemplate") JmsTemplate jmsQueueTemplate,
                           BatchFileHelper batchFileHelper,
                           ExportedSignatureDataService exportedSignatureDataService,
                           JdbcTemplate jdbcTemplate) {
        this.signatureDataRepository = signatureDataRepository;
        this.signDataMapper = signDataMapper;
        this.objectMapper = objectMapper;
        this.signDataSchemaValidator = signDataSchemaValidator;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.batchFileHelper = batchFileHelper;
        this.exportedSignatureDataService = exportedSignatureDataService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public SignatureDataUbmEntity saveSignData(SignatureDataDTO signatureDataDto) throws JsonProcessingException {
        String signDataAsJsonString = objectMapper.writeValueAsString(signatureDataDto);
        signDataSchemaValidator.validateJsonData(signDataAsJsonString);
        SignatureDataUbmEntity signatureDataUbmEntity = signDataMapper.mapToSignDataEntity(signatureDataDto, signDataAsJsonString);
        return signatureDataRepository.save(signatureDataUbmEntity);
    }

    public void saveSignDataInBatch(List<SignatureDataDTO> signatureDataDtoList) throws JsonProcessingException {

        List<Object[]> batch = new ArrayList<>();
        Date savedAt = new Date();
        for (int i = 0; i < signatureDataDtoList.size(); i++) {
            SignatureDataDTO signatureDataDTO = signatureDataDtoList.get(i);
            String signDataAsJsonString = objectMapper.writeValueAsString(signatureDataDTO);
            signDataSchemaValidator.validateJsonData(signDataAsJsonString);
            //SignatureDataUbmEntity signatureDataUbmEntity = signDataMapper.mapToSignDataEntity(signatureDataDTO, signDataAsJsonString);
            batch.add(new Object[]{signatureDataDTO.getSignatureId(), signatureDataDTO.getTimestamp(), savedAt, signDataAsJsonString});
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO " + SIGNATURE_DATA_UBM_TABLE + " (signature_id, signed_at, saved_at, signature_data_json) VALUES (?, ?, ?, ?)", batch);

    }

    @Transactional
    public void exportArchiveAndDelete() throws IOException {

        Instant startCount = Instant.now();

        int numberOfSignatureDataToExport = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + SIGNATURE_DATA_UBM_TABLE, Integer.class);

        Instant endCount = Instant.now();
        System.out.println("Count elapsed Time: " + Duration.between(startCount, endCount).toString());

        log.info("Number of sign data to export: " + numberOfSignatureDataToExport);
        if(numberOfSignatureDataToExport == 0) {
            // since no signature data was fetched, no data will be exported.
            return;
        }

        boolean useGzip = numberOfSignatureDataToExport > GZIP_TRESHOLD;

        File tempFile = batchFileHelper.createTempFile(useGzip);

        Instant startFetchWriteAndDelete = Instant.now();

        try (
                OutputStream fileOut = new FileOutputStream(tempFile);
                OutputStream bufferedOut = new BufferedOutputStream(fileOut, 256 * 1024);
                OutputStream maybeGzipOut = useGzip ? new GZIPOutputStream(bufferedOut) : bufferedOut;
                Writer writer = new BufferedWriter(new OutputStreamWriter(maybeGzipOut, StandardCharsets.UTF_8), 256 * 1024)
        ) {
            List<Long> idBatch = new ArrayList<>();
            List<Object[]> archiveBatch = new ArrayList<>();
            Date exportedAt = new Date();

            jdbcTemplate.query("SELECT id, signature_id, signed_at, signature_data_json FROM " + SIGNATURE_DATA_UBM_TABLE, (ResultSet resultSet) -> {
                do {
                    Long id = resultSet.getLong("id");
                    String signatureId = resultSet.getString("signature_id");
                    Date signedAt = resultSet.getDate("signed_at");
                    String signatureDataJson = resultSet.getString("signature_data_json");

                    // 1. Skriv till fil
                    try {
                        writer.write(signatureDataJson + System.lineSeparator());
                        writer.flush();
                    } catch (IOException exception) {
                        log.error("Error when trying to write sign data to file {}, got exception message: {}", tempFile, exception.getMessage());
                        throw new RuntimeException(exception);
                    }


                    // 2. Förbered för arkivering
                    archiveBatch.add(new Object[]{signatureId, signedAt, exportedAt});

                    // 3. Förbered för radering
                    idBatch.add(id);

                    // 4. Skicka batch till arkiv + töm buffers
                    if (archiveBatch.size() >= ARCHIVE_BATCH_SIZE) {
                        archiveExportedSignatures(archiveBatch);
                        archiveBatch.clear();
                    }

                    if (idBatch.size() >= DELETE_BATCH_SIZE) {
                        deleteFromSignatureDataTable(idBatch);
                        idBatch.clear();
                    }
                } while(resultSet.next());

                // sista batcharna
                if (!archiveBatch.isEmpty()) archiveExportedSignatures(archiveBatch);
                if (!idBatch.isEmpty()) deleteFromSignatureDataTable(idBatch);
            });
        }

        Instant endFetchWriteAndDelete = Instant.now();
        System.out.println("FETCH_WRITE_DELETE elapsed Time: " + Duration.between(startFetchWriteAndDelete, endFetchWriteAndDelete).toString());

        // 5. Skicka till kön
        Instant startExport = Instant.now();

        jmsQueueTemplate.send("sign-data-export-ubm", session -> {
            BytesMessage message = session.createBytesMessage();
            try (InputStream input = new BufferedInputStream(new FileInputStream(tempFile), 256 * 1024)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    message.writeBytes(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            message.setBooleanProperty("gzip", useGzip);
            message.setStringProperty("filename", tempFile.getName());
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            return message;
        });

        Instant endExport = Instant.now();
        System.out.println("EXPORT elapsed Time: " + Duration.between(startExport, endExport).toString());

        Instant startDeleteFile = Instant.now();
        tempFile.delete();
        Instant endDeleteFile = Instant.now();
        System.out.println("DELETE_FILE elapsed Time: " + Duration.between(startDeleteFile, endDeleteFile).toString());

        System.out.printf("✅ Export + arkivering + radering klar (%d rader, gzip=%s)%n", numberOfSignatureDataToExport, useGzip);
    }

    private void archiveExportedSignatures(List<Object[]> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO " + EXPORTED_SIGNATURE_DATA_TABLE + " (signature_id, signed_at, exported_at) VALUES (?, ?, ?)", batch);
    }

    private void deleteFromSignatureDataTable(List<Long> ids) {
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        jdbcTemplate.update("DELETE FROM " + SIGNATURE_DATA_UBM_TABLE + " WHERE id IN (" + placeholders + ")",
                ids.toArray());
    }


    @Transactional
    public void exportSignData() throws IOException {

        /*
                // Anta att vi hämtar JSON-objekt strömmande från en databas eller API
                while (databaseResultSet.next()) {
                    SignDataEntity entity = mapRowToEntity(databaseResultSet);
                    jsonGenerator.writeRawValue(objectMapper.writeValueAsString(entity));
                    writer.write("\n");
                    writer.flush();
                }
             */
        List<SignatureDataUbmEntity> signatureDataUbmEntityList = signatureDataRepository.findAll();
        // todo: kanske använda datum från den senaste sign data ist?
        OffsetDateTime now = OffsetDateTime.now();

        int numberOfRecords = signatureDataUbmEntityList.size();
        log.info("Number of sign data fetched: " + numberOfRecords);
        if(numberOfRecords == 0) {
            // since no signature data was fetched, no data will be exported.
            return;
        }

        //Create batch file with sign data and get file content
        String fileName = batchFileHelper.writeSignDataJsonToBatchFile(signatureDataUbmEntityList);
        byte[] fileContent = batchFileHelper.getFileContent(fileName);

        //Send file content to queue
        jmsQueueTemplate.convertAndSend("sign-data-export-ubm", fileContent);
        log.info("filinnehåll för {} skickad till kö {} ", fileName, "sign-data-export-ubm");

        //Delete file
        Files.delete(Path.of(fileName));
        log.info("Temp file is deleted");

        //Delete all fetched sign data records from DB
        //signatureDataRepository.deleteAllBeforeNow(now);
        deleteInBatchByIds(signatureDataUbmEntityList);
        log.info("List of SignatureDataEntity is deleted");

        //Save id for all exported signature data to be able to resend data in future
        exportedSignatureDataService.saveExportedSignatureIdsInBulk(signatureDataUbmEntityList);
        log.info("Ids for all exported signature data is saved");
    }

    private void deleteInBatchByIds(List<SignatureDataUbmEntity> signatureDataUbmEntityList) {
        int batchSize = 1000;
        List<Long> ids = signatureDataUbmEntityList.stream()
                .map(SignatureDataUbmEntity::getId)
                .collect(Collectors.toList());

        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batch = ids.subList(i, end);

            jdbcTemplate.batchUpdate(
                    "DELETE FROM signature_data_ubm WHERE id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, batch.get(i));
                        }

                        @Override
                        public int getBatchSize() {
                            return batch.size();
                        }
                    }
            );
        }
    }

}
