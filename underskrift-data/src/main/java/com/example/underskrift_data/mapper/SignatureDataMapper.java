package com.example.underskrift_data.mapper;

import com.example.underskrift_data.models.dto.SignatureDataDTO;
import com.example.underskrift_data.models.entity.SignatureDataEntity;
import org.springframework.stereotype.Component;

@Component
public class SignatureDataMapper {

    public SignatureDataEntity mapToSignatureDataEntity(SignatureDataDTO signatureDataDto) {
        return SignatureDataEntity.builder()
                .signatureId(signatureDataDto.getSignatureId())
                .ipAddress(signatureDataDto.getIpAddress())
                .personalNumber(signatureDataDto.getPersonalNumber())
                .status(signatureDataDto.getStatus().name())
                .timestamp(signatureDataDto.getTimestamp())
                .statusMessage(signatureDataDto.getStatusMessage())
                .build();
    }
}
