package com.example.underskrift_export.scheduler;

import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.SignDataRepository;
import com.example.underskrift_export.services.SignDataService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ExportSignDataScheduler {

    private final SignDataService signDataService;


    @Scheduled(fixedRateString = "${exportSignData.interval:100000}", initialDelay = 10000)
    @Async // Execute in a separate thread
    public void exportSignData() {

        try {
            signDataService.exportSignData();
        } catch (Exception exception) {
            log.error("Export of signature data went wrong: " + exception);
        }
    }

}
