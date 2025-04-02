package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.ExportedSignDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExportedSignatureDataService {

    private final ExportedSignDataRepository exportedSignDataRepository;

    public ExportedSignatureDataService(ExportedSignDataRepository exportedSignDataRepository) {
        this.exportedSignDataRepository = exportedSignDataRepository;
    }

    @Transactional
    public void saveExportedSignatureIdsInBulk(List<SignDataEntity> signDataEntityList) {
        OffsetDateTime exportedAt = OffsetDateTime.now();

        for (int i = 0; i < signDataEntityList.size(); i++) {
            SignDataEntity signDataEntity = signDataEntityList.get(i);
            exportedSignDataRepository.saveExportedSignatureId(
                    signDataEntity.getSignatureId(),
                    signDataEntity.getSignedAt(),
                    exportedAt
            );
        }

    }
}
