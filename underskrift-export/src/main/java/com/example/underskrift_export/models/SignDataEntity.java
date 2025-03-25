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
@Table(name = "signature_data")
@Builder
public class SignDataEntity {

    @Id
    @Column(name = "sign_id")
    private String signId;

    private OffsetDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "personal_number")
    private String personalNumber;

    private String status;
}
