package com.example.underskrift_export.utils;

import com.example.underskrift_export.models.SignatureDataUbmEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class BatchFileHelper {

    public File createTempFile(boolean useGzip) {

        /*String signatureDataPath = "signatureData";
        Path tmpdir = Files.createTempDirectory(Paths.get("target"), "tmpDirPrefix");
        Path tmpSignDataPath = Paths.get(System.getProperty("java.io.tmpdir"), signatureDataPath);
        if(Files.notExists(tmpSignDataPath)){
            try {
                Path tempDirectory = Files.createTempDirectory(signatureDataPath).;
            } catch (IOException exception) {
                log.error("Error when trying to create directory {}, got exception message: {}", tmpSignDataPath, exception.getMessage());
                throw new RuntimeException(exception);
            }
        }*/

        String extension = useGzip ? ".gz" : ".csv";
        String formattedDateTime = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));
        String fileName =  "sign_data_" + formattedDateTime;
        try {
            return Files.createTempFile(fileName, extension).toFile();
        } catch (IOException exception) {
            log.error("Error when trying to temp file {}, got exception message: {}", fileName+extension, exception.getMessage());
            throw new RuntimeException(exception);
        }

    }

    public String writeSignDataJsonToBatchFile(List<SignatureDataUbmEntity> signatureDataUbmEntityList) {
        //JsonFactory jsonFactory = objectMapper.getFactory();
        Path tmpSignDataPath = Paths.get("signdata");
        if(Files.notExists(tmpSignDataPath)){
            try {
                Files.createDirectory(tmpSignDataPath);
            } catch (IOException exception) {
                log.error("Error when trying to create directory {}, got exception message: {}", tmpSignDataPath, exception.getMessage());
                throw new RuntimeException(exception.getMessage());
            }
        }

        String formattedDateTime = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));
        String fileName =  tmpSignDataPath + File.separator + "sign_data_" + formattedDateTime + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))
             //todo use jsonGenerator
             //JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer)
        ) {

            // Strömma JSON-objekten rad för rad
            for (int i = 0; i < signatureDataUbmEntityList.size(); i++) {
                SignatureDataUbmEntity signatureDataUbmEntity = signatureDataUbmEntityList.get(i);
                //jsonGenerator.writeRawValue(objectMapper.writeValueAsString());

                writer.write(signatureDataUbmEntity.getSignatureDataJson() + System.lineSeparator());
                writer.flush(); // Tvinga skrivning direkt (strömmande)
            }
            log.info("batchfile with sign data created: " + fileName);
        } catch (Exception exception) {
            log.error("Error when trying to write sign data to file {}, got exception message: {}", fileName, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
        return fileName;
    }

    public byte[] getFileContent(String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            return fileInputStream.readAllBytes();
        } catch (Exception exception) {
            log.error("Error when trying to read file content for file {}. Exception message: {}", fileName, exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }
}
