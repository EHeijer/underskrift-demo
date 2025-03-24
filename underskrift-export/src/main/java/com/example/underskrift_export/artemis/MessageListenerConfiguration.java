package com.example.underskrift_export.artemis;

import com.example.artemis_configuration.ArtemisListenerSetting;
import com.example.artemis_configuration.CustomJmsListenerConfigurer;
import com.example.artemis_configuration.JmsListenerSupport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MessageListenerConfiguration {

    @Bean
    public CustomJmsListenerConfigurer customJmsListenerConfigurer(JmsListenerSupport jmsListenerSupport, @Qualifier("signDataSubscriberListener") SignDataSubscriberListener SignDataSubscriberListener) {
        return jmsListenerSupport.customJmsListenerConfigurer(List.of(
                new ArtemisListenerSetting(
                        true,
                        "sign-data-topic",
                        "sign-data-subscriber",
                        SignDataSubscriberListener
                )
        ));
    }
}
