package com.example.underskrift_export.artemis;

import com.example.underskrift_export.models.SignDataDto;
import com.example.underskrift_export.services.SignDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SignDataSubscriberListener implements MessageListener {

//    @JmsListener(
//            destination = "sign-data-topic",
//            subscription = "sign-data-subscriber",
//            containerFactory = "topicListenerContainerFactory"
//    )
//    public void onMessage(String message) {
//        System.out.println("Received message: " + message);
//        // Additional business logic can be added here
//    }

    private final SignDataService signDataService;
    private final ObjectMapper objectMapper;

    public SignDataSubscriberListener(SignDataService signDataService, ObjectMapper objectMapper) {
        this.signDataService = signDataService;
        this.objectMapper = objectMapper;
    }

    @Override
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
    }

    private SignDataDto convertToSignDataDto(BytesMessage bytesMessage) throws JMSException, IOException {
        byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(bytes);

        SignDataDto signDataDto = objectMapper.readValue(bytes, SignDataDto.class);
        return signDataDto;
    }
}
