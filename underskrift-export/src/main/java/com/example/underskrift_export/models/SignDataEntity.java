package com.example.underskrift_export.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "signature_data")
@Builder
public class SignDataEntity {

    @Id
    @Column(name = "signature_id")
    private String signatureId;

    @Column(name = "signed_at")
    private OffsetDateTime signedAt;

    @Column(name = "saved_at")
    private OffsetDateTime savedAt;

    @Column(name = "signature_data_json")
    private String signatureDataJson;

    /*@Id
    @Column(name = "sign_id")
    private String signId;

    private OffsetDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "personal_number")
    private String personalNumber;

    private String status;*/
}
