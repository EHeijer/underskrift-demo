package com.example.underskrift_data.controller;

import com.example.underskrift_data.models.SignDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SignDataController {


    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/sign-data")
    public String sendMessage(@RequestBody SignDataDto signData) {


        try {
            jmsTemplate.convertAndSend("test-queue", objectMapper.writeValueAsString(signData));
        } catch (JsonProcessingException e) {
            log.error("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        log.info("Sign data saved to DB and sent to queue");
        return "Message sent!";
    }
}
