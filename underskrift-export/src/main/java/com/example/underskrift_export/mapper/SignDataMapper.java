package com.example.underskrift_export.mapper;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.models.SignatureDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Slf4j
public class SignDataMapper {

    public SignatureDataEntity mapToSignDataEntity(SignatureDataDTO signatureDataDto, String signDataAsJsonString) {

        SignatureDataEntity signatureDataEntity = SignatureDataEntity.builder()
                .signatureId(signatureDataDto.getSignatureId())
                .signedAt(signatureDataDto.getTimestamp())
                .savedAt(OffsetDateTime.now())
                .signatureDataJson(signDataAsJsonString)
                .build();
        return signatureDataEntity;

    }
}
