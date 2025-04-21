package com.example.underskrift_export.scheduler;

import com.example.underskrift_export.services.SignDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ExportSignDataScheduler {

    private final SignDataService signDataService;


    @Scheduled(fixedRateString = "${exportSignData.interval:1000000}", initialDelay = 10000)
    @Async // Execute in a separate thread
    public void exportSignData() {

        try {
            Instant start = Instant.now();
            log.info("Export of sign data START: " + start);

            //signDataService.exportSignData();
            signDataService.exportArchiveAndDelete();

            Instant end = Instant.now();
            log.info("Export of sign data END: " + end);
            System.out.println("TOTAL elapsed Time: " + Duration.between(start, end).toString());
        } catch (Exception exception) {
            log.error("Export of signature data went wrong: " + exception);
        }
    }

}
