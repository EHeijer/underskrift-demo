package com.example.underskrift_export.mapper;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.models.SignatureDataUbmEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Date;

@Component
@Slf4j
public class SignDataMapper {

    public SignatureDataUbmEntity mapToSignDataEntity(SignatureDataDTO signatureDataDto, String signDataAsJsonString) {

        SignatureDataUbmEntity signatureDataUbmEntity = SignatureDataUbmEntity.builder()
                .signatureId(signatureDataDto.getSignatureId())
                .signedAt(signatureDataDto.getTimestamp())
                .savedAt(new Date())
                .signatureDataJson(signDataAsJsonString)
                .build();
        return signatureDataUbmEntity;

    }
}
