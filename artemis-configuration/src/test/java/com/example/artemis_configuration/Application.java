package com.example.artemis_configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

@SpringBootApplication
@EnableJms
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }

    public static String resultMessage;
    @JmsListener(
            destination = "testQueue1"
            //containerFactory = "queueListenerContainerFactory"
    )
    public void onTestMessage(String message) {
        resultMessage = message;
    }
}
