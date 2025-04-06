package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignatureDataEntity;
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
    public void saveExportedSignatureIdsInBulk(List<SignatureDataEntity> signatureDataEntityList) {
        OffsetDateTime exportedAt = OffsetDateTime.now();

        for (int i = 0; i < signatureDataEntityList.size(); i++) {
            SignatureDataEntity signatureDataEntity = signatureDataEntityList.get(i);
            exportedSignDataRepository.saveExportedSignatureId(
                    signatureDataEntity.getSignatureId(),
                    signatureDataEntity.getSignedAt(),
                    exportedAt
            );
        }

    }
}
