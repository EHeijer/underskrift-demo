package com.example.artemis_configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

@Configuration
@EnableJms
@EnableConfigurationProperties(ArtemisConnectionProperties.class)
@ConditionalOnProperty(
        prefix = "spring.artemis.embedded",
        name = {"enabled"},
        havingValue = "false",
        matchIfMissing = true
)
public class CustomArtemisConfiguration {

    @Bean
    public ConnectionFactory artemisConnectionFactory(ArtemisConnectionProperties artemisConnectionProperties) throws JMSException {
        return new ArtemisConnectionFactory(artemisConnectionProperties).createArtemisConnectionFactory();
    }

    @Bean
    public MessageConverter customMessageConverter(ObjectMapper objectMapper) {
        return new CustomMessageConverter(objectMapper);
    }

    @Bean
    public JmsTemplate jmsQueueTemplate(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory) {
        return new JmsTemplateSupport(artemisConnectionFactory, new SimpleMessageConverter()).createJmsQueueTemplate();
    }

    @Bean
    public JmsTemplate jmsTopicTemplate(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory) {
        return new JmsTemplateSupport(artemisConnectionFactory, new SimpleMessageConverter()).createJmsTopicTemplate();
    }

    @Bean
    public DefaultJmsListenerContainerFactory queueListenerContainerFactory(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory) throws JMSException {
        return new JmsListenerContainerFactory(artemisConnectionFactory, new SimpleMessageConverter()).createQueueListenerContainerFactory();
    }

    @Bean
    public DefaultJmsListenerContainerFactory topicListenerContainerFactory(@Qualifier("artemisConnectionFactory") ConnectionFactory artemisConnectionFactory) throws JMSException {
        return new JmsListenerContainerFactory(artemisConnectionFactory, new SimpleMessageConverter()).createTopicListenerContainerFactory();
    }

    @Bean
    public JmsListenerSupport jmsListenerSupport(DefaultJmsListenerContainerFactory queueListenerContainerFactory, DefaultJmsListenerContainerFactory topicListenerContainerFactory) {
        return new JmsListenerSupport(queueListenerContainerFactory, topicListenerContainerFactory);
    }

}
