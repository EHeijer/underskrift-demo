package com.example.underskrift_export.mapper;

import com.example.underskrift_export.models.SignDataDto;
import com.example.underskrift_export.models.SignDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Slf4j
public class SignDataMapper {

    public SignDataEntity mapToSignDataEntity(SignDataDto signDataDto, String signDataAsJsonString) {

        SignDataEntity signDataEntity = SignDataEntity.builder()
                .signatureId(signDataDto.getSignId())
                .signedAt(signDataDto.getTimestamp())
                .savedAt(OffsetDateTime.now())
                .signatureDataJson(signDataAsJsonString)
                .build();
        return signDataEntity;

    }
}
