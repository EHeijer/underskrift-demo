package com.example.underskrift_export.repositories;

import com.example.underskrift_export.models.ExportedSignDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface ExportedSignDataRepository extends JpaRepository<ExportedSignDataEntity, String> {

    @Modifying
    @Query(value = "INSERT INTO exported_signature_data (signature_id, signed_at, exported_at) VALUES (:signatureId, :signedAt, :exportedAt)", nativeQuery = true)
    void saveExportedSignatureId(
            @Param("signatureId") String signatureId,
            @Param("signedAt") OffsetDateTime signedAt,
            @Param("exportedAt") OffsetDateTime exportedAt
    );
}
