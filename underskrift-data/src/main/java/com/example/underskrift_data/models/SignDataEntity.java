package com.example.underskrift_data.models;

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
