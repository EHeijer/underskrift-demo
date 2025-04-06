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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

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

    public SignDataService(SignatureDataRepository signatureDataRepository,
                           SignDataMapper signDataMapper,
                           ObjectMapper objectMapper,
                           SignDataSchemaValidator signDataSchemaValidator,
                           @Qualifier("jmsQueueTemplate") JmsTemplate jmsQueueTemplate,
                           BatchFileHelper batchFileHelper,
                           ExportedSignatureDataService exportedSignatureDataService) {
        this.signatureDataRepository = signatureDataRepository;
        this.signDataMapper = signDataMapper;
        this.objectMapper = objectMapper;
        this.signDataSchemaValidator = signDataSchemaValidator;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.batchFileHelper = batchFileHelper;
        this.exportedSignatureDataService = exportedSignatureDataService;
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

        //Delete all fetched sign data records from DB
        signatureDataRepository.deleteAllBeforeNow(now);

        //Save id for all exported signature data to be able to resend data in future
        exportedSignatureDataService.saveExportedSignatureIdsInBulk(signatureDataEntityList);
    }

}
