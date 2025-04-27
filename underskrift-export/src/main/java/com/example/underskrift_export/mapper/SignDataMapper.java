package com.example.underskrift_export.mapper;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.models.SignatureDataUbmEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public SignatureDataDTO mapToSignatureDataDTO(ResultSet resultSet) throws SQLException {
        return SignatureDataDTO.builder()
                .signatureId(resultSet.getString("signature_id"))
                .timestamp(resultSet.getTimestamp("timestamp"))
                .ipAddress(resultSet.getString("ip_address"))
                .personalNumber(resultSet.getString("personal_number"))
                .status(SignatureDataDTO.Status.valueOf(resultSet.getString("status")))
                .statusMessage(resultSet.getString("status_message"))
                .build();
    }
}
