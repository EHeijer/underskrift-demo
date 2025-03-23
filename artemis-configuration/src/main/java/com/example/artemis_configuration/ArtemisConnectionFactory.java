package com.example.artemis_configuration;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtemisConnectionFactory {

    private final ArtemisConnectionProperties artemisConnectionProperties;

    public ArtemisConnectionFactory(ArtemisConnectionProperties artemisConnectionProperties) {
        this.artemisConnectionProperties = artemisConnectionProperties;
    }

    public ConnectionFactory createArtemisConnectionFactory() throws JMSException {

        List<ArtemisConnectionProperties.Broker> brokers = artemisConnectionProperties.brokers();
        // todo: kasta exception om brokers saknas
        // todo: kasta exception om user/password saknas

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(true, getTransportConfigurations(brokers));

        connectionFactory.setUser(artemisConnectionProperties.username());
        connectionFactory.setPassword(artemisConnectionProperties.password());

        // todo reconnect config, max connections

        return connectionFactory;
    }

    private TransportConfiguration[] getTransportConfigurations(List<ArtemisConnectionProperties.Broker> brokers) {
        List<TransportConfiguration> transportConfigurations = new ArrayList<>();

        for(ArtemisConnectionProperties.Broker broker : brokers) {
            Map<String, Object> transportParams = new HashMap<>();

            // todo: kasta exception om host/port saknas
            transportParams.put("host", broker.host());
            transportParams.put("port", broker.port());
            transportParams.put("sslEnabled", broker.sslEnabled() != null ? broker.sslEnabled() : true);

            transportConfigurations.add(new TransportConfiguration(NettyConnectorFactory.class.getName(), transportParams));

        }

        return transportConfigurations.toArray(TransportConfiguration[]::new);
    }
}
