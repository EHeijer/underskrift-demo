package com.example.underskrift_data.services;

import com.example.underskrift_data.mapper.SignatureDataMapper;
import com.example.underskrift_data.models.dto.SignatureDataDTO;
import com.example.underskrift_data.models.entity.SignatureDataEntity;
import com.example.underskrift_data.repositories.SignatureDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignatureDataService {

    private final SignatureDataRepository signatureDataRepository;
    private final JmsTemplate jmsTopicTemplate;
    private final ObjectMapper objectMapper;
    private final SignatureDataMapper signatureDataMapper;

    public SignatureDataService(SignatureDataRepository signatureDataRepository, @Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate, ObjectMapper objectMapper, SignatureDataMapper signatureDataMapper) {
        this.signatureDataRepository = signatureDataRepository;
        this.jmsTopicTemplate = jmsTopicTemplate;
        this.objectMapper = objectMapper;
        this.signatureDataMapper = signatureDataMapper;
    }

    public void sendSignatureDataEvent(SignatureDataDTO signatureDataDto) throws JsonProcessingException {
        jmsTopicTemplate.convertAndSend("signature-data-topic", objectMapper.writeValueAsBytes(signatureDataDto));
    }

    @Transactional
    public void saveSignatureData(SignatureDataDTO signatureDataDto) {
        SignatureDataEntity signatureDataEntity = signatureDataMapper.mapToSignatureDataEntity(signatureDataDto);
        signatureDataRepository.save(signatureDataEntity);
    }

}
