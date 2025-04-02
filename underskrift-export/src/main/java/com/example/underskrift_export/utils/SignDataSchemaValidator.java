package com.example.underskrift_export.utils;

import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SignDataSchemaValidator {

    private final Schema signatureDataSchema;

    public SignDataSchemaValidator(@Qualifier("signatureDataSchema") Schema signatureDataSchema) {
        this.signatureDataSchema = signatureDataSchema;
    }

    public void validateJsonData(String jsonDataString) {
        // validera json-datan mot schemat
        JSONObject jsonData = new JSONObject(new JSONTokener(jsonDataString));
        signatureDataSchema.validate(jsonData);
    }

}
