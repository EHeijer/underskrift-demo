package com.example.underskrift_export.services;

import com.example.underskrift_export.mapper.SignDataMapper;

import com.example.underskrift_export.models.SignatureDataEntity;
import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.repositories.SignatureDataRepository;
import com.example.underskrift_export.utils.BatchFileHelper;
import com.example.underskrift_export.utils.SignDataSchemaValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SignDataService {

    private final SignatureDataRepository signatureDataRepository;
    private final SignDataMapper signDataMapper;
    private final ObjectMapper objectMapper;
    private final SignDataSchemaValidator signDataSchemaValidator;
    private final JmsTemplate jmsQueueTemplate;
    private final BatchFileHelper batchFileHelper;
    private final ExportedSignatureDataService exportedSignatureDataService;
    private final JdbcTemplate jdbcTemplate;

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

    public SignatureDataEntity saveSignData(SignatureDataDTO signatureDataDto) throws JsonProcessingException {
        String signDataAsJsonString = objectMapper.writeValueAsString(signatureDataDto);
        signDataSchemaValidator.validateJsonData(signDataAsJsonString);
        SignatureDataEntity signatureDataEntity = signDataMapper.mapToSignDataEntity(signatureDataDto, signDataAsJsonString);
        return signatureDataRepository.save(signatureDataEntity);
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
        List<SignatureDataEntity> signatureDataEntityList = signatureDataRepository.findAll();
        // todo: kanske använda datum från den senaste sign data ist?
        OffsetDateTime now = OffsetDateTime.now();

        int numberOfRecords = signatureDataEntityList.size();
        log.info("Number of sign data fetched: " + numberOfRecords);
        if(numberOfRecords == 0) {
            // since no signature data was fetched, no data will be exported.
            return;
        }

        //Create batch file with sign data and get file content
        String fileName = batchFileHelper.writeSignDataJsonToBatchFile(signatureDataEntityList);
        byte[] fileContent = batchFileHelper.getFileContent(fileName);

        //Send file content to queue
        jmsQueueTemplate.convertAndSend("sign-data-export-ubm", fileContent);
        log.info("filinnehåll för {} skickad till kö {} ", fileName, "sign-data-export-ubm");

        //Delete file
        Files.delete(Path.of(fileName));
        log.info("Temp file is deleted");

        //Delete all fetched sign data records from DB
        //signatureDataRepository.deleteAllBeforeNow(now);
        deleteInBatchByIds(signatureDataEntityList);
        log.info("List of SignatureDataEntity is deleted");

        //Save id for all exported signature data to be able to resend data in future
        exportedSignatureDataService.saveExportedSignatureIdsInBulk(signatureDataEntityList);
        log.info("Ids for all exported signature data is saved");
    }

    private void deleteInBatchByIds(List<SignatureDataEntity> signatureDataEntityList) {
        int batchSize = 1000;
        List<Long> ids = signatureDataEntityList.stream()
                .map(SignatureDataEntity::getId)
                .collect(Collectors.toList());

        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batch = ids.subList(i, end);

            jdbcTemplate.batchUpdate(
                    "DELETE FROM signature_data WHERE id = ?",
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
