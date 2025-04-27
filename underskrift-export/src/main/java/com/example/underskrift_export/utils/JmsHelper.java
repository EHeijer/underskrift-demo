package com.example.underskrift_export.utils;

import jakarta.jms.BytesMessage;
import jakarta.jms.DeliveryMode;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class JmsHelper {

    private final JmsTemplate jmsQueueTemplate;

    public JmsHelper(JmsTemplate jmsQueueTemplate) {
        this.jmsQueueTemplate = jmsQueueTemplate;
    }

    public void streamFileContentToQueue(String destination, File tempFile) {
        jmsQueueTemplate.send(destination, session -> {
            BytesMessage message = session.createBytesMessage();
            try (InputStream input = new BufferedInputStream(new FileInputStream(tempFile), 256 * 1024)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    message.writeBytes(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            message.setStringProperty("filename", tempFile.getName());
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            return message;
        });
    }
}
