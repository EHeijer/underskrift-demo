package com.example.underskrift_export.artemis;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.services.SignDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SignatureDataSubscriberListener {

    private final SignDataService signDataService;
    private final ObjectMapper objectMapper;

    public SignatureDataSubscriberListener(SignDataService signDataService, ObjectMapper objectMapper) {
        this.signDataService = signDataService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(
            destination = "signature-data-topic",
            subscription = "underskrift-export-subscriber",
            containerFactory = "topicListenerContainerFactory"
    )
    public void onMessage(byte[] message) {

        SignatureDataDTO signatureDataDto = null;
        try {
            signatureDataDto = readAsSignDataDto(message);
            log.info("Received signData: " + signatureDataDto);
            signDataService.saveSignData(signatureDataDto);
        } catch (Exception exception) {
            log.error("Something went wrong when trying to save incoming signature data {}. Error message: {}", signatureDataDto, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    private SignatureDataDTO readAsSignDataDto(byte[] bytesMessage) throws IOException {
        SignatureDataDTO signatureDataDto = objectMapper.readValue(bytesMessage, SignatureDataDTO.class);
        return signatureDataDto;
    }

     /*@Override
    public void onMessage(Message message) {

        try {
            if (message instanceof BytesMessage bytesMessage) {
                SignDataDto signDataDto = convertToSignDataDto(bytesMessage);
                System.out.println("Received signData: " + signDataDto);
                signDataService.saveSignData(signDataDto);
            } else {
                throw new IllegalArgumentException("Unexptected type");
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }*/
}
