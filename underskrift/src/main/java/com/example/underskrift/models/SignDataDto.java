package com.example.underskrift.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SignDataDto {

    private String signId;
    private Instant timestamp;
    private String ipAddress;
    private String personalNumber;
    private Status status;

    public enum Status {
        SUCCESS, FAILURE, CANCEL
    }
}
