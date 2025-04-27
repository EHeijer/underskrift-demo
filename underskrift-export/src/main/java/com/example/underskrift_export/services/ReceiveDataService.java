package com.example.underskrift_export.services;

import com.example.underskrift_export.mapper.SignDataMapper;
import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.models.SignatureDataUbmEntity;
import com.example.underskrift_export.repositories.SignatureDataRepository;
import com.example.underskrift_export.utils.SignDataSchemaValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ReceiveDataService {

    public static final String SIGNATURE_DATA_UBM_TABLE = "signature_data_ubm";
    private final SignatureDataRepository signatureDataRepository;
    private final SignDataMapper signDataMapper;
    private final ObjectMapper objectMapper;
    private final SignDataSchemaValidator signDataSchemaValidator;
    private final JdbcTemplate jdbcTemplate;

    public ReceiveDataService(SignatureDataRepository signatureDataRepository,
                              SignDataMapper signDataMapper,
                              ObjectMapper objectMapper,
                              SignDataSchemaValidator signDataSchemaValidator,
                              JdbcTemplate jdbcTemplate) {
        this.signatureDataRepository = signatureDataRepository;
        this.signDataMapper = signDataMapper;
        this.objectMapper = objectMapper;
        this.signDataSchemaValidator = signDataSchemaValidator;
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


}
