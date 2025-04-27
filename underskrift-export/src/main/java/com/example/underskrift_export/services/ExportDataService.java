package com.example.underskrift_export.services;

import com.example.underskrift_export.mapper.SignDataMapper;
import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.models.SignatureDataUbmEntity;
import com.example.underskrift_export.models.SyncRequest;
import com.example.underskrift_export.repositories.SignatureDataRepository;
import com.example.underskrift_export.utils.BatchFileHelper;
import com.example.underskrift_export.utils.JmsHelper;
import com.example.underskrift_export.utils.SignDataSchemaValidator;
import com.example.underskrift_export.utils.SqlWithParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.DeliveryMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
@Slf4j
public class ExportDataService {

    public static final String SIGNATURE_DATA_UBM_TABLE = "signature_data_ubm";
    public static final String EXPORTED_SIGNATURE_DATA_TABLE = "exported_signature_data";
    public static final int GZIP_TRESHOLD = 150000;
    private static final int DELETE_BATCH_SIZE = 1000;
    private static final int ARCHIVE_BATCH_SIZE = 1000;
    private static final int MAX_ROWS_PER_FILE = 500_000;
    private final SignatureDataRepository signatureDataRepository;
    private final JmsTemplate jmsQueueTemplate;
    private final BatchFileHelper batchFileHelper;
    private final JdbcTemplate jdbcTemplate;
    private final SignDataMapper signDataMapper;
    private final ObjectMapper objectMapper;
    private final SignDataSchemaValidator signDataSchemaValidator;
    private final JmsHelper jmsHelper;

    @Value("${export-queue:sign-data-export-ubm}")
    private String exportQueueName;

    public ExportDataService(SignatureDataRepository signatureDataRepository,
                             JmsTemplate jmsQueueTemplate,
                             BatchFileHelper batchFileHelper,
                             JdbcTemplate jdbcTemplate, SignDataMapper signDataMapper, ObjectMapper objectMapper, SignDataSchemaValidator signDataSchemaValidator, JmsHelper jmsHelper) {
        this.signatureDataRepository = signatureDataRepository;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.batchFileHelper = batchFileHelper;
        this.jdbcTemplate = jdbcTemplate;
        this.signDataMapper = signDataMapper;
        this.objectMapper = objectMapper;
        this.signDataSchemaValidator = signDataSchemaValidator;
        this.jmsHelper = jmsHelper;
    }

    @Transactional
    public void exportArchiveAndDelete() throws IOException {

        File tempFile = batchFileHelper.createTempFile();

        Instant startFetchWriteAndDelete = Instant.now();

        try (
                OutputStream fileOut = new FileOutputStream(tempFile);
                OutputStream outputStream = new GZIPOutputStream(new BufferedOutputStream(fileOut, 256 * 1024));
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 256 * 1024)
        ) {

            jdbcTemplate.query(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "SELECT id, signature_id, signed_at, signature_data_json FROM " + SIGNATURE_DATA_UBM_TABLE,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                );
                //För PostgreSQL är dessaa krav för strömning att fungera:
                con.setAutoCommit(false);
                ps.setFetchSize(1000);
                return ps;
            }, (ResultSet resultSet) -> {

                List<Long> idBatch = new ArrayList<>(DELETE_BATCH_SIZE);
                List<Object[]> archiveBatch = new ArrayList<>(ARCHIVE_BATCH_SIZE);
                Date exportedAt = new Date();

                while (resultSet.next()) {
                    Long id = resultSet.getLong("id");
                    String signatureId = resultSet.getString("signature_id");
                    Date signedAt = resultSet.getDate("signed_at");
                    String signatureDataJson = resultSet.getString("signature_data_json");

                    // 1. Skriv till fil
                    try {
                        writer.write(signatureDataJson + System.lineSeparator());
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
                }

                // sista batcharna
                if (!archiveBatch.isEmpty()) archiveExportedSignatures(archiveBatch);
                if (!idBatch.isEmpty()) deleteFromSignatureDataTable(idBatch);
                return null;
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

        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Trigga GC för mer exakt mätning

        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        double usedMemoryMB = usedMemoryBytes / (1024.0 * 1024);
        log.info("🔍 Used heap memory: {} MB", String.format("%.2f", usedMemoryMB));
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
        jmsQueueTemplate.convertAndSend(exportQueueName, fileContent);
        log.info("filinnehåll för {} skickad till kö {} ", fileName, "sign-data-export-ubm");

        //Delete file
        Files.delete(Path.of(fileName));
        log.info("Temp file is deleted");

        //Delete all fetched sign data records from DB
        //signatureDataRepository.deleteAllBeforeNow(now);
        deleteInBatchByIds(signatureDataUbmEntityList);
        log.info("List of SignatureDataEntity is deleted");

        //Save id for all exported signature data to be able to resend data in future
        saveExportedSignatureIdsInBulk(signatureDataUbmEntityList);
        log.info("Ids for all exported signature data is saved");
    }

    private void saveExportedSignatureIdsInBulk(List<SignatureDataUbmEntity> signatureDataUbmEntityList) {
        String sql = "INSERT INTO exported_signature_data (signature_id, signed_at, exported_at) VALUES (?, ?, ?)";
        //                                                      1             2           3              1  2  3

        OffsetDateTime exportedAt = OffsetDateTime.now();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SignatureDataUbmEntity signatureDataUbmEntity = signatureDataUbmEntityList.get(i);
                ps.setString(1, signatureDataUbmEntity.getSignatureId());
                ps.setObject(2, signatureDataUbmEntity.getSignedAt()); // Använder OffsetDateTime
                ps.setObject(3, exportedAt);
            }

            public int getBatchSize() {
                return signatureDataUbmEntityList.size();
            }
        });

//        for (int i = 0; i < signatureDataEntityList.size(); i++) {
//            SignatureDataEntity signatureDataEntity = signatureDataEntityList.get(i);
//            exportedSignDataRepository.saveExportedSignatureId(
//                    signatureDataEntity.getSignatureId(),
//                    signatureDataEntity.getSignedAt(),
//                    exportedAt
//            );
//        }

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

    @Async
    public void syncSignatureDataFromSource(SyncRequest syncRequest) {

        try {
            log.info("Starting sync...");

            // 1. Evaluate SQL and params
            SqlWithParams sqlWithParams = evaluateSqlFromRequest(syncRequest);

            // 2. Execute query
            jdbcTemplate.query(con -> {
                PreparedStatement ps = con.prepareStatement(
                        sqlWithParams.getSql(),
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                );
                //För PostgreSQL är dessaa krav för strömning att fungera:
                con.setAutoCommit(false);
                ps.setFetchSize(1000);

                // set param values
                List<Object> params = sqlWithParams.getParams();
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                return ps;
            }, (ResultSet resultSet) -> {

                List<Object[]> archiveBatch = new ArrayList<>(ARCHIVE_BATCH_SIZE);
                Date exportedAt = new Date();
                int currentRowInFile = 0;
                File tempFile = null;
                Writer writer = null;

                // 3. Loop through result set
                while (resultSet.next()) {
                    try {

                        if(currentRowInFile == 0) {
                            tempFile = batchFileHelper.createTempFile();
                            OutputStream fileOut = new FileOutputStream(tempFile);
                            OutputStream outputStream = new GZIPOutputStream(new BufferedOutputStream(fileOut, 256 * 1024));
                            writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 256 * 1024);
                        }

                        // Map resultSet to SignatureDataDTO and validate the json
                        SignatureDataDTO signatureDataDTO = signDataMapper.mapToSignatureDataDTO(resultSet);
                        String signDataAsJsonString = objectMapper.writeValueAsString(signatureDataDTO);
                        signDataSchemaValidator.validateJsonData(signDataAsJsonString);

                        try {
                            writer.write(signDataAsJsonString + System.lineSeparator());
                        } catch (IOException exception) {
                            log.error("Error when trying to write sign data to temp file: ", exception);
                            throw new RuntimeException(exception);
                        }

                        currentRowInFile++;

                        // Förbered för arkivering
                        archiveBatch.add(new Object[]{signatureDataDTO.getSignatureId(), signatureDataDTO.getTimestamp(), exportedAt});

                        // Skicka batch till arkiv(exported_signature_data) + töm batchlista
                        if (archiveBatch.size() >= ARCHIVE_BATCH_SIZE) {
                            archiveExportedSignatures(archiveBatch);
                            archiveBatch.clear();
                        }


                        if(currentRowInFile >= MAX_ROWS_PER_FILE){

                            //close writer
                            batchFileHelper.flushAndClose(writer);

                            //send to queue
                            jmsHelper.streamFileContentToQueue(exportQueueName, tempFile);

                            //remove old temp file
                            if(!tempFile.delete()) {
                                log.error("Could not delete tempFile");
                            }

                            currentRowInFile = 0;
                        }


                    } catch (Exception e) {

                        throw new RuntimeException("Error handling result set:", e);
                    }
                }


                //
                if(currentRowInFile > 0) {
                    batchFileHelper.flushAndClose(writer);
                    jmsHelper.streamFileContentToQueue(exportQueueName, tempFile);

                    if(!tempFile.delete()) {
                        log.error("Could not delete tempFile");
                    }
                }

                // sista batcharna
                if (!archiveBatch.isEmpty()) {
                    archiveExportedSignatures(archiveBatch);
                    archiveBatch.clear();
                }


                return null; // Eftersom vi strömmar och skriver direkt, vi returnerar inget.
            });

        } catch (Exception exception){
            log.error("Sync error: ", exception);
        }

    }

    private SqlWithParams evaluateSqlFromRequest(SyncRequest syncRequest) {
        StringBuilder sql = new StringBuilder("SELECT * FROM signature_data WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(syncRequest.isSyncAll()) {
            sql.append(" ORDER BY id");
            return SqlWithParams.builder()
                    .sql(sql.toString())
                    .params(params)
                    .build();
        }

        if (syncRequest.getFromDate() != null) {
            sql.append(" AND timestamp >= ?");
            params.add(java.sql.Date.valueOf(syncRequest.getFromDate()));
        }
        if (syncRequest.getToDate() != null) {
            sql.append(" AND timestamp <= ?");
            params.add(java.sql.Date.valueOf(syncRequest.getToDate()));
        }

        sql.append(" ORDER BY id");
        return SqlWithParams.builder()
                .sql(sql.toString())
                .params(params)
                .build();
    }
}
