package com.example.artemis_configuration;

import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import java.util.List;

public class JmsListenerSupport {

    private final DefaultJmsListenerContainerFactory queueListenerContainerFactory;
    private final DefaultJmsListenerContainerFactory topicListenerContainerFactory;

    public JmsListenerSupport(DefaultJmsListenerContainerFactory queueListenerContainerFactory, DefaultJmsListenerContainerFactory topicListenerContainerFactory) {
        this.queueListenerContainerFactory = queueListenerContainerFactory;
        this.topicListenerContainerFactory = topicListenerContainerFactory;
    }

    public CustomJmsListenerConfigurer customJmsListenerConfigurer(List<ArtemisListenerSetting> artemisListenerSettings) {
        return new CustomJmsListenerConfigurer(artemisListenerSettings, queueListenerContainerFactory, topicListenerContainerFactory);
    }
}

