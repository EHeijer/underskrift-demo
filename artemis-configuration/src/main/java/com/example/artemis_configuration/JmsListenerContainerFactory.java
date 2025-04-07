package com.example.artemis_configuration;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.support.converter.SimpleMessageConverter;

public class JmsListenerContainerFactory {

    private final ConnectionFactory artemisConnectionFactory;
    private final SimpleMessageConverter simpleMessageConverter;

    public JmsListenerContainerFactory(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory, SimpleMessageConverter simpleMessageConverter) {
        this.artemisConnectionFactory = artemisConnectionFactory;
        this.simpleMessageConverter = simpleMessageConverter;
    }

    public DefaultJmsListenerContainerFactory createTopicListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(artemisConnectionFactory);
        factory.setPubSubDomain(true);
        factory.setSubscriptionShared(true);
        factory.setSubscriptionDurable(true);
        factory.setTransactionManager(getTransactionManager());
        factory.setMessageConverter(simpleMessageConverter);
        //factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);
        // todo: factory.setErrorHandler();
        return factory;
    }

    public DefaultJmsListenerContainerFactory createQueueListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(artemisConnectionFactory);
        factory.setPubSubDomain(false);
        factory.setTransactionManager(getTransactionManager());
        //factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);
        factory.setMessageConverter(simpleMessageConverter);
        // todo: factory.setErrorHandler();
        return factory;
    }

    @Bean
    public JmsTransactionManager getTransactionManager() throws JMSException {
        return new JmsTransactionManager(artemisConnectionFactory);
    }
}
