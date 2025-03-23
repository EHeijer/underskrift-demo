package com.example.artemis_configuration;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;

public class JmsListenerContainerFactory {

    private final ConnectionFactory artemisConnectionFactory;

    public JmsListenerContainerFactory(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory) {
        this.artemisConnectionFactory = artemisConnectionFactory;
    }

    public DefaultJmsListenerContainerFactory createTopicListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(artemisConnectionFactory);
        factory.setPubSubDomain(true);
        factory.setSubscriptionShared(true);
        factory.setSubscriptionDurable(true);
        factory.setTransactionManager(getTransactionManager());
        //factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);
        // todo: factory.setErrorHandler();
        return factory;
    }

    public DefaultJmsListenerContainerFactory createQueueListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(artemisConnectionFactory);
        factory.setPubSubDomain(true);
        factory.setSubscriptionShared(true);
        factory.setSubscriptionDurable(true);
        factory.setTransactionManager(getTransactionManager());
        //factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);
        // todo: factory.setErrorHandler();
        return factory;
    }

    @Bean
    public JmsTransactionManager getTransactionManager() throws JMSException {
        return new JmsTransactionManager(artemisConnectionFactory);
    }
}
