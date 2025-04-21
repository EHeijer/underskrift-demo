package com.example.underskrift_export.tmp;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.DeliveryMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@EnableScheduling
public class SignatureDataProducer {

    private final JmsTemplate jmsTopicTemplate;
    private final ObjectMapper objectMapper;

    public SignatureDataProducer(@Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate, ObjectMapper objectMapper) {
        this.jmsTopicTemplate = jmsTopicTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRateString = "${sendSignData.interval:10000}")
    @Async // Execute in a separate thread
    public void exportSignData() throws JsonProcessingException {

        List<SignatureDataDTO> signatureDataDTOList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {

            SignatureDataDTO signData = SignatureDataDTO.builder()
                    .signatureId(UUID.randomUUID().toString())
                    .ipAddress("0.0.0.0")
                    .personalNumber("199109113978")
                    .status(SignatureDataDTO.Status.SUCCESS)
                    .timestamp(new Date())
                    .build();

            signatureDataDTOList.add(signData);
        }

        //jmsTopicTemplate.convertAndSend("signature-data-topic", objectMapper.writeValueAsBytes(signData));
        jmsTopicTemplate.send("signature-data-topic", session -> {
            BytesMessage message = session.createBytesMessage();
            /*try (InputStream input = new BufferedInputStream(new FileInputStream(tempFile), 256 * 1024)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    message.writeBytes(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }*/

            //todo
            /*for (int i = 0; i < signatureDataDTOList.size(); i++) {
                SignatureDataDTO signatureDataDTO = signatureDataDTOList.get(i);
                try {
                    message.writeBytes(objectMapper.writeValueAsBytes(signatureDataDTO));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }*/

            try {
                message.writeBytes(objectMapper.writeValueAsBytes(signatureDataDTOList));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }


            message.setBooleanProperty("isBatch", true);
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            return message;
        });

    }
}
