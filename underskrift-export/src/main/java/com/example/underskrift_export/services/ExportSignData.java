package com.example.underskrift_export.services;

import com.example.underskrift_export.models.SignDataEntity;
import com.example.underskrift_export.repositories.SignDataRepository;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.internal.JsonPointerFormatValidator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ExportSignData {


    private final SignDataRepository signDataRepository;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsQueueTemplate;

    @Scheduled(fixedRateString = "${exportSignData.interval:100000}", initialDelay = 10000)
    @Async // Execute in a separate thread
    public void sendSignData() {

        try {

            JSONObject schemaAsJsonObject = loadJsonSchema("schema/SignatureDataUbmV1.json");
            // Ladda JSON-schema och skapa validator
            Schema schema = SchemaLoader.load(schemaAsJsonObject);
            /*
                // Anta att vi hämtar JSON-objekt strömmande från en databas eller API
                while (databaseResultSet.next()) {
                    SignDataEntity entity = mapRowToEntity(databaseResultSet);
                    jsonGenerator.writeRawValue(objectMapper.writeValueAsString(entity));
                    writer.write("\n");
                    writer.flush();
                }
             */
            List<SignDataEntity> signDataEntityList = signDataRepository.findAll();
            log.info("Number of sign data fetched: " + signDataEntityList.size());

            JsonFactory jsonFactory = objectMapper.getFactory();

            String formattedDateTime = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));
            String fileName = "sign_data_" + formattedDateTime + ".csv";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                 //todo use jsonGenerator
                 JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer)) {

                // Strömma JSON-objekten rad för rad
                for (int i = 0; i < signDataEntityList.size(); i++) {
                    SignDataEntity signDataEntity = signDataEntityList.get(i);
                    //jsonGenerator.writeRawValue(objectMapper.writeValueAsString());
                    String jsonString = objectMapper.writeValueAsString(signDataEntity);
                    // Validera JSON mot schema
                    // Ladda JSON-data
                    JSONObject jsonData = new JSONObject(new JSONTokener(jsonString));
                    schema.validate(jsonData);

                    writer.write(jsonString + System.lineSeparator());
                    writer.flush(); // Tvinga skrivning direkt (strömmande)
                }
                log.info("fil med strömmande JSON-rader skapad: " + fileName);
            } catch (Exception exception) {
                log.error("Error when trying to write sign data to file {}, got exception message: {}", fileName, exception.getMessage());
                throw new RuntimeException(exception.getMessage());
            }

            try (FileInputStream fileInputStream = new FileInputStream(fileName)) {

                // Läs in hela filen till en byte-array
                byte[] fileContent = fileInputStream.readAllBytes();

                // Skicka binärmeddelandet till Artemis
                jmsQueueTemplate.convertAndSend("sign-data-export-ubm", fileContent);
                log.info("filinnehåll för {} skickad till kö {} ", fileName, "sign-data-export-ubm");

            } catch (Exception exception) {
                log.error("Error when trying to read file content for file {} and send to queue, got exception message: {}", fileName, exception.getMessage());
                throw new RuntimeException(exception.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject loadJsonSchema(String schemaFileName) {

        try (InputStream inputStream = JsonPointerFormatValidator.class.getClassLoader().getResourceAsStream(schemaFileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Kunde inte hitta filen: " + schemaFileName);
            }
            return new JSONObject(new JSONTokener(inputStream));
        } catch (Exception e) {
            throw new RuntimeException("Fel vid laddning av JSON-schema: " + e.getMessage(), e);
        }
    }

}
