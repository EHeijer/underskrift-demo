package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignatureDataUbmEntity;
import com.example.underskrift_export.repositories.ExportedSignDataRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExportedSignatureDataService {

    private final ExportedSignDataRepository exportedSignDataRepository;
    private final JdbcTemplate jdbcTemplate;

    public ExportedSignatureDataService(ExportedSignDataRepository exportedSignDataRepository, JdbcTemplate jdbcTemplate) {
        this.exportedSignDataRepository = exportedSignDataRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveExportedSignatureIdsInBulk(List<SignatureDataUbmEntity> signatureDataUbmEntityList) {
        String sql = "INSERT INTO exported_signature_data (signature_id, signed_at, exported_at) VALUES (?, ?, ?)";
        //                                                      1             2           3              1  2  3

        OffsetDateTime exportedAt = OffsetDateTime.now();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SignatureDataUbmEntity signatureDataUbmEntity = signatureDataUbmEntityList.get(i);
                ps.setString(1, signatureDataUbmEntity.getSignatureId());
                ps.setObject(2, signatureDataUbmEntity.getSignedAt()); // Använder OffsetDateTime
                ps.setObject(3, exportedAt);
            }

            public int getBatchSize() {
                return signatureDataUbmEntityList.size();
            }
        });

//        for (int i = 0; i < signatureDataEntityList.size(); i++) {
//            SignatureDataEntity signatureDataEntity = signatureDataEntityList.get(i);
//            exportedSignDataRepository.saveExportedSignatureId(
//                    signatureDataEntity.getSignatureId(),
//                    signatureDataEntity.getSignedAt(),
//                    exportedAt
//            );
//        }

    }
}
