package com.example.artemis_configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "artemis.connection")
public record ArtemisConnectionProperties(String username, String password, List<Broker> brokers) {

    record Broker(String host, int port, Boolean sslEnabled) {

    }
}
