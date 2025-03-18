package com.example.artemis_configuration;

import jakarta.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
@EnableJms
public class ArtemisConfiguration {

    @Value("${spring.artemis.broker-url:amqp://localhost:61616}")
    private String brokerUrl;

    @Value("${spring.artemis.user:user}")
    private String username;

    @Value("${spring.artemis.password:password}")
    private String password;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl); // tcp: for TCP protocol
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public JmsTransactionManager transactionManager() throws JMSException {
        return new JmsTransactionManager(connectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplate() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setTransactionManager(transactionManager());
        factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);
        factory.setMessageConverter(customMessageConverter());
        return factory;
    }

    @Bean
    public MessageConverter customMessageConverter() {
        return new CustomMessageConverter();
    }

}
