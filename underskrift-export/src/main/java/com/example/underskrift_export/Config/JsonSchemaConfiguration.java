package com.example.underskrift_export.Config;

import org.everit.json.schema.Schema;
import org.everit.json.schema.internal.JsonPointerFormatValidator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class JsonSchemaConfiguration {

    @Bean
    public Schema signatureDataSchema() {
        JSONObject schemaAsJsonObject = loadJsonSchema("schema/SignatureDataUbmV1.json");
        return SchemaLoader.load(schemaAsJsonObject);
    }

    private JSONObject loadJsonSchema(String schemaFileName) {

        try (InputStream inputStream = JsonPointerFormatValidator.class.getClassLoader().getResourceAsStream(schemaFileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find file: " + schemaFileName);
            }
            return new JSONObject(new JSONTokener(inputStream));
        } catch (Exception e) {
            throw new RuntimeException("Error when loading JSON-schema: " + e.getMessage(), e);
        }
    }
}
