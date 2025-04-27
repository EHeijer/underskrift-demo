package com.example.underskrift_export.tmp;

import com.example.underskrift_export.models.SignatureDataDTO;
import com.example.underskrift_export.services.ReceiveDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SignatureDataExportListener {

    private final ReceiveDataService receiveDataService;
    private final ObjectMapper objectMapper;

    public SignatureDataExportListener(ReceiveDataService receiveDataService, ObjectMapper objectMapper) {
        this.receiveDataService = receiveDataService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(
            destination = "sign-data-export-ubm",
            containerFactory = "queueListenerContainerFactory"
    )
    public void onMessage(BytesMessage message) {

        try {
            log.info("ICC Received message");

            String filename = message.getStringProperty("filename");
            byte[] payloadAsBytes = message.getBody(byte[].class);

            Path path = Paths.get("output/" + filename);
            // Skapa kataloger om de inte finns
            Files.createDirectories(path.getParent());

            Files.write(path, payloadAsBytes);

            log.info("Fil skapad: " + filename);
        } catch (Exception exception) {
            log.error("Something went wrong when trying to save incoming signature data {}. Error message: {}", message, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    private SignatureDataDTO readAsSignDataDto(byte[] payloadAsBytes) throws IOException {
        SignatureDataDTO signatureDataDto = objectMapper.readValue(payloadAsBytes, SignatureDataDTO.class);
        return signatureDataDto;
    }

    private List<SignatureDataDTO> readAsSignDataDtoList(byte[] payloadAsBytes) throws IOException {
        SignatureDataDTO[] signatureDataDTOS = objectMapper.readValue(payloadAsBytes, SignatureDataDTO[].class);
        return Arrays.stream(signatureDataDTOS).toList();
    }

     /*@Override
    public void onMessage(Message message) {

        try {
            if (message instanceof BytesMessage bytesMessage) {
                SignDataDto signDataDto = convertToSignDataDto(bytesMessage);
                System.out.println("Received signData: " + signDataDto);
                signDataService.saveSignData(signDataDto);
            } else {
                throw new IllegalArgumentException("Unexptected type");
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }*/
}
