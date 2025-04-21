package com.example.underskrift_export.artemis;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.services.SignDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void onMessage(BytesMessage message) {

        //SignatureDataDTO signatureDataDto = null;
        try {
            log.info("Received message: " + message);
            boolean isBatch = message.getBooleanProperty("isBatch");
            //byte[] payloadAsBytes = new byte[(int) message.getBodyLength()];

            if(isBatch) {
                //List<SignatureDataDTO> signatureDataDtoList = message.getBody(List.class);
                //byte[] payloadAsBytes = new byte[(int) message.getBodyLength()];'
                byte[] payloadAsBytes = message.getBody(byte[].class);
                List<SignatureDataDTO> signatureDataDtoList = readAsSignDataDtoList(payloadAsBytes);
                signDataService.saveSignDataInBatch(signatureDataDtoList);
            } else {
                SignatureDataDTO signatureDataDto = message.getBody(SignatureDataDTO.class);
                //SignatureDataDTO signatureDataDto = readAsSignDataDto(payloadAsBytes);
                signDataService.saveSignData(signatureDataDto);
            }
            log.info("signature data saved to DB");
        } catch (Exception exception) {
            log.error("Something went wrong when trying to save incoming signature data {}. Error message: {}", message, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    private SignatureDataDTO readAsSignDataDto(byte[] payloadAsBytes) throws IOException {
        SignatureDataDTO signatureDataDto = objectMapper.readValue(payloadAsBytes, SignatureDataDTO.class);
        return signatureDataDto;
    }

    private List<SignatureDataDTO> readAsSignDataDtoList(byte[] payloadAsBytes) throws IOException {
        SignatureDataDTO[] signatureDataDTOS = objectMapper.readValue(payloadAsBytes, SignatureDataDTO[].class);
        return Arrays.stream(signatureDataDTOS).toList();
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
