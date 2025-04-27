package com.example.underskrift.service;

import com.example.underskrift.models.SignatureDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class SendSignData {


    private final RestTemplate restTemplate;

    @Scheduled(fixedRateString = "${sendSignData.interval:1000}")
    @Async // Execute in a separate thread
    public void sendSignData() {

        SignatureDataDTO signData = SignatureDataDTO.builder()
                .signatureId(UUID.randomUUID().toString())
                .ipAddress("0.0.0.0")
                .personalNumber("199109113978")
                .status(SignatureDataDTO.Status.SUCCESS)
                .timestamp(new Date())
                .build();

        restTemplate.postForEntity("http://localhost:8081/signature-data", signData, String.class);

        log.info("Sign data sent");
    }

}
