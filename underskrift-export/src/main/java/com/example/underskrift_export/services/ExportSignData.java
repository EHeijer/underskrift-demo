package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.SignDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ExportSignData {


    private final SignDataRepository signDataRepository;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsQueueTemplate;

    @Scheduled(fixedRateString = "${exportSignData.interval:100000}", initialDelay = 100000)
    @Async // Execute in a separate thread
    public void sendSignData() {

        try {
            List<SignDataEntity> signDataEntityList = signDataRepository.findAll();
            log.info("Number of sign data fetched: " + signDataEntityList.size());

            // todo map to SignatureDataUbmV1
//            for (SignDataEntity signDataEntity : signDataEntityList) {
//
//            }

            jmsQueueTemplate.convertAndSend("sign-data-export-ubm",  objectMapper.writeValueAsBytes(signDataEntityList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
