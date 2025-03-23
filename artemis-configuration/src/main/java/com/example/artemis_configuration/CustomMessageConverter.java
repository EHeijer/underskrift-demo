package com.example.artemis_configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

public class CustomMessageConverter implements MessageConverter {

    private final SimpleMessageConverter delegate = new SimpleMessageConverter();
    private final ObjectMapper objectMapper;

    public CustomMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, 	MessageConversionException {
        // Add custom conversion logic if needed
        try {
            return delegate.toMessage(objectMapper.writeValueAsBytes(object), session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error when trying to convert to JMS message");
        }
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        // Add custom conversion logic if needed
        return delegate.fromMessage(message);
    }
}
