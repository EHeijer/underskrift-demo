package com.example.artemis_configuration;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

public class JmsTemplateSupport {

    private final ConnectionFactory artemisConnectionFactory;
    private final MessageConverter customMessageConverter;

    public JmsTemplateSupport(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory, @Qualifier("customMessageConverter") MessageConverter customMessageConverter) {
        this.artemisConnectionFactory = artemisConnectionFactory;
        this.customMessageConverter = customMessageConverter;
    }

    public JmsTemplate createJmsQueueTemplate(){

        JmsTemplate jmsTemplate = new JmsTemplate(artemisConnectionFactory);
        jmsTemplate.setMessageConverter(customMessageConverter);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setPubSubDomain(false);

        return jmsTemplate;
    }

    public JmsTemplate createJmsTopicTemplate(){

        JmsTemplate jmsTemplate = new JmsTemplate(artemisConnectionFactory);
        jmsTemplate.setMessageConverter(customMessageConverter);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setPubSubDomain(true);

        return jmsTemplate;
    }
}
