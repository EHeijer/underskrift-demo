package com.example.underskrift_data.services;

import com.example.underskrift_data.models.SignDataDto;
import com.example.underskrift_data.models.SignDataEntity;
import com.example.underskrift_data.repositories.SignDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignDataService {

    private final SignDataRepository signDataRepository;
    private final JmsTemplate jmsTopicTemplate;
    private final ObjectMapper objectMapper;

    public SignDataService(SignDataRepository signDataRepository, @Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate, ObjectMapper objectMapper) {
        this.signDataRepository = signDataRepository;
        this.jmsTopicTemplate = jmsTopicTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSignDataEvent(SignDataDto signDataDto) throws JsonProcessingException {
        jmsTopicTemplate.convertAndSend("sign-data-topic", objectMapper.writeValueAsBytes(signDataDto));
    }

    @Transactional
    public void saveSignData(SignDataDto signDataDto) {
        SignDataEntity signDataEntity = SignDataEntity.builder()
                .signId(signDataDto.getSignId())
                .ipAddress(signDataDto.getIpAddress())
                .personalNumber(signDataDto.getPersonalNumber())
                .status(signDataDto.getStatus().name())
                .timestamp(signDataDto.getTimestamp())
                .build();

        signDataRepository.save(signDataEntity);
    }
}
