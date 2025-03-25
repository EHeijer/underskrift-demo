package com.example.underskrift_export.services;

import com.example.underskrift_export.generated.SignatureDataUbmV1;
import com.example.underskrift_export.models.SignDataDto;
import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.SignDataRepository;
import org.springframework.stereotype.Service;

@Service
public class SignDataService {

    private final SignDataRepository signDataRepository;

    public SignDataService(SignDataRepository signDataRepository) {
        this.signDataRepository = signDataRepository;
    }

    public void saveSignData(SignDataDto signDataDto) {
        SignDataEntity signDataEntity = mapToSignDataEntity(signDataDto);
        signDataRepository.save(signDataEntity);
    }

    private SignDataEntity mapToSignDataEntity(SignDataDto signDataDto) {
        SignDataEntity signDataEntity = SignDataEntity.builder()
                .signId(signDataDto.getSignId())
                .ipAddress(signDataDto.getIpAddress())
                .personalNumber(signDataDto.getPersonalNumber())
                .status(signDataDto.getStatus().name())
                .timestamp(signDataDto.getTimestamp())
                .build();
        return signDataEntity;
    }
}
