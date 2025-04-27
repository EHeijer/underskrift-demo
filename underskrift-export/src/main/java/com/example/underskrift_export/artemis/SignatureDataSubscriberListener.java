package com.example.underskrift_export.artemis;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.services.ReceiveDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SignatureDataSubscriberListener {

    private final ReceiveDataService receiveDataService;
    private final ObjectMapper objectMapper;

    public SignatureDataSubscriberListener(ReceiveDataService receiveDataService, ObjectMapper objectMapper) {
        this.receiveDataService = receiveDataService;
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
            byte[] payloadAsBytes = message.getBody(byte[].class);

            if(isBatch) {
                //List<SignatureDataDTO> signatureDataDtoList = message.getBody(List.class);
                //byte[] payloadAsBytes = new byte[(int) message.getBodyLength()];'
                List<SignatureDataDTO> signatureDataDtoList = readAsSignDataDtoList(payloadAsBytes);
                receiveDataService.saveSignDataInBatch(signatureDataDtoList);
            } else {
                SignatureDataDTO signatureDataDto = readAsSignDataDto(payloadAsBytes);
                receiveDataService.saveSignData(signatureDataDto);
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
