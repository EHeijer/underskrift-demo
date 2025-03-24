package com.example.artemis_configuration;

import jakarta.jms.MessageListener;

public record ArtemisListenerSetting(boolean isSubscriber, String destinationName, String subscriberName, MessageListener messageListener) {
}
