package com.example.underskrift_data.models.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SignatureDataDTO {

    private String signatureId;
    private Date timestamp;
    private String ipAddress;
    private String personalNumber;
    private Status status;
    private String statusMessage;

    public enum Status {
        SUCCESS, FAILURE, CANCEL
    }
}
