package com.example.artemis_configuration;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

public class JmsTemplateSupport {

    private final ConnectionFactory artemisConnectionFactory;
    private final SimpleMessageConverter simpleMessageConverter;

    public JmsTemplateSupport(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory, SimpleMessageConverter simpleMessageConverter) {
        this.artemisConnectionFactory = artemisConnectionFactory;
        this.simpleMessageConverter = simpleMessageConverter;
    }

    public JmsTemplate createJmsQueueTemplate(){

        JmsTemplate jmsTemplate = new JmsTemplate(artemisConnectionFactory);
        jmsTemplate.setMessageConverter(simpleMessageConverter);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setPubSubDomain(false);

        return jmsTemplate;
    }

    public JmsTemplate createJmsTopicTemplate(){

        JmsTemplate jmsTemplate = new JmsTemplate(artemisConnectionFactory);
        jmsTemplate.setMessageConverter(simpleMessageConverter);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setPubSubDomain(true);

        return jmsTemplate;
    }
}
