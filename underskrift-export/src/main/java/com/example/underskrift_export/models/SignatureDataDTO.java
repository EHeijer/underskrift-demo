package com.example.underskrift_export.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SignatureDataDTO {

    private String signatureId;
    private OffsetDateTime timestamp;
    private String ipAddress;
    private String personalNumber;
    private Status status;
    private String statusMessage;

    public enum Status {
        SUCCESS, FAILURE, CANCEL
    }
}
