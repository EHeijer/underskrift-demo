package com.example.artemis_configuration;

import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArtemisConfig implements ArtemisConfigurationCustomizer {
    @Override
    public void customize(org.apache.activemq.artemis.core.config.Configuration configuration) {
        try {
            configuration.addAcceptorConfiguration("remote", "tcp://127.0.0.1:61616");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
