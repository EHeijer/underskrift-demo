package com.example.underskrift_export.artemis;

import com.example.underskrift_export.models.SignDataDto;
import com.example.underskrift_export.services.SignDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SignDataSubscriberListener {

    private final SignDataService signDataService;
    private final ObjectMapper objectMapper;

    public SignDataSubscriberListener(SignDataService signDataService, ObjectMapper objectMapper) {
        this.signDataService = signDataService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(
            destination = "sign-data-topic",
            subscription = "sign-data-subscriber",
            containerFactory = "topicListenerContainerFactory"
    )
    public void onMessage(byte[] message) {

        SignDataDto signDataDto = null;
        try {
            signDataDto = readAsSignDataDto(message);
            log.info("Received signData: " + signDataDto);
            signDataService.saveSignData(signDataDto);
        } catch (Exception exception) {
            log.error("Something went wrong when trying to save incoming signature data {}. Error message: {}", signDataDto, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    private SignDataDto readAsSignDataDto(byte[] bytesMessage) throws JMSException, IOException {
//        byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
//        bytesMessage.readBytes(bytes);

        SignDataDto signDataDto = objectMapper.readValue(bytesMessage, SignDataDto.class);
        return signDataDto;
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
