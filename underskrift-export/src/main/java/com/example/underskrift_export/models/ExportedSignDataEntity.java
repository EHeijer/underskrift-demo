package com.example.underskrift_export.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exported_signature_data")
@Builder
public class ExportedSignDataEntity {

    @Id
    @Column(name = "signature_id")
    private String signatureId;

    @Column(name = "signed_at")
    private OffsetDateTime signedAt;

    @Column(name = "exported_at")
    private OffsetDateTime exportedAt;

}
