package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.SignDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(fixedRateString = "${exportSignData.interval:10000}")
    @Async // Execute in a separate thread
    public void sendSignData() {

        List<SignDataEntity> signDataEntityList = signDataRepository.findAll();

        log.info("Number of sign data to send: " + signDataEntityList.size());
    }

}
