package com.example.underskrift_data.artemis;

import com.example.underskrift_data.models.dto.SignatureDataDTO;
import com.example.underskrift_data.services.SignatureDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
@Slf4j
public class SignatureDataSubscriberListener {

    private final SignatureDataService signatureDataService;
    private final ObjectMapper objectMapper;

    public SignatureDataSubscriberListener(SignatureDataService signatureDataService, ObjectMapper objectMapper) {
        this.signatureDataService = signatureDataService;
        this.objectMapper = objectMapper;
    }


    @JmsListener(
            destination = "signature-data-topic",
            subscription = "underskrift-data-subscriber",
            containerFactory = "topicListenerContainerFactory"
    )
    public void onSignatureDataMessage(byte[] message) {

        SignatureDataDTO signatureDataDTO = null;
        try {
            signatureDataDTO = readAsSignatureDataDTO(message);
            log.info("Received signData: " + signatureDataDTO);
            signatureDataService.saveSignatureData(signatureDataDTO);
        } catch (Exception exception) {
            log.error("Something went wrong when trying to save incoming signature data {}. Error: {}", signatureDataDTO, exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    private SignatureDataDTO readAsSignatureDataDTO(byte[] bytesMessage) throws JMSException, IOException {
        return objectMapper.readValue(bytesMessage, SignatureDataDTO.class);
    }

}
