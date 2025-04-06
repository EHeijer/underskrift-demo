package com.example.underskrift_data.models.entity;

import jakarta.persistence.*;
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
public class SignatureDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "signature_id", nullable = false)
    private String signatureId;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "personal_number", nullable = false)
    private String personalNumber;

    @Column(nullable = false)
    private String status;

    //Används endast om status inte är success
    @Column(name = "status_message")
    private String statusMessage;
}
