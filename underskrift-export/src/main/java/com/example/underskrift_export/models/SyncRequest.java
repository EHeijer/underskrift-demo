package com.example.underskrift_export.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SyncRequest {

    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean syncAll = false;
}
