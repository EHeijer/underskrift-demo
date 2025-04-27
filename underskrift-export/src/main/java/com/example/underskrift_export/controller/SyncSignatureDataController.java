package com.example.underskrift_export.controller;

import com.example.underskrift_export.models.SyncRequest;
import com.example.underskrift_export.services.ExportDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncSignatureDataController {

    private final ExportDataService exportDataService;

    @PostMapping("/signature-data")
    public ResponseEntity<String> triggerSyncFromSource(@RequestBody SyncRequest syncRequest) {

        if((syncRequest.getFromDate() != null || syncRequest.getToDate() != null) && syncRequest.isSyncAll()) {
            return ResponseEntity
                    .badRequest()
                    .body("'syncAll' can't be set to true while fromDate/toDate is set");
        }
        exportDataService.syncSignatureDataFromSource(syncRequest);

        log.info("Sync of from signature_data table has been triggered. SyncRequest: {}", syncRequest);
        return ResponseEntity.ok("Sync is triggered!!");

    }
}
