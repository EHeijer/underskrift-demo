package com.example.underskrift_export.artemis;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @JmsListener(
            destination = "sign-data-topic",
            subscription = "sign-data-subscriber",
            containerFactory = "topicListenerContainerFactory"
    )
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        // Additional business logic can be added here
    }
}
