package com.example.artemis_configuration;

import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import java.util.List;

public class CustomJmsListenerConfigurer implements JmsListenerConfigurer {

    private final List<ArtemisListenerSetting> artemisListenerSettings;
    private final DefaultJmsListenerContainerFactory queueListenerContainerFactory;
    private final DefaultJmsListenerContainerFactory topicListenerContainerFactory;

    public CustomJmsListenerConfigurer(List<ArtemisListenerSetting> artemisListenerSettings,
                                       DefaultJmsListenerContainerFactory queueListenerContainerFactory,
                                       DefaultJmsListenerContainerFactory topicListenerContainerFactory) {
        this.artemisListenerSettings = artemisListenerSettings;
        this.queueListenerContainerFactory = queueListenerContainerFactory;
        this.topicListenerContainerFactory = topicListenerContainerFactory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {

        for(ArtemisListenerSetting artemisListenerSetting : artemisListenerSettings) {

            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();

            endpoint.setId(artemisListenerSetting.destinationName());
            endpoint.setDestination(artemisListenerSetting.destinationName());
            endpoint.setMessageListener(artemisListenerSetting.messageListener());

            if(artemisListenerSetting.isSubscriber()) {
                endpoint.setSubscription(artemisListenerSetting.subscriberName());
                registrar.registerEndpoint(endpoint, topicListenerContainerFactory);
            } else {
                registrar.registerEndpoint(endpoint, queueListenerContainerFactory);
            }
        }
    }
}
