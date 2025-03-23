package com.example.underskrift_data.controller;

import com.example.underskrift_data.models.SignDataDto;
import com.example.underskrift_data.services.SignDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SignDataController {


    private final SignDataService signDataService;

    public SignDataController(SignDataService signDataService) {
        this.signDataService = signDataService;
    }

    @PostMapping("/sign-data")
    public String sendMessage(@RequestBody SignDataDto signDataDto) {


        try {
            signDataService.saveSignData(signDataDto);
            signDataService.sendSignDataEvent(signDataDto);
        } catch (Exception e) {
            log.error("Could not handle incoming sign data: " + signDataDto.getSignId());
            // todo create error audit event here
            throw new RuntimeException("Error while trying to handle incoming sign data, got message: "+ e.getMessage());
        }

        log.info("Sign data saved to DB and sent to topic");
        return "Success!!";
    }
}
