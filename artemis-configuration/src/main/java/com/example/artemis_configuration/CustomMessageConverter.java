package com.example.artemis_configuration;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

public class CustomMessageConverter implements MessageConverter {

    private final SimpleMessageConverter delegate = new SimpleMessageConverter();

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, 	MessageConversionException {
        // Add custom conversion logic if needed
        return delegate.toMessage(object, session);
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        // Add custom conversion logic if needed
        return delegate.fromMessage(message);
    }
}
