package com.example.underskrift_data.controller;

import com.example.underskrift_data.models.dto.SignatureDataDTO;
import com.example.underskrift_data.services.SignatureDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/signature-data")
public class SignatureDataController {


    private final SignatureDataService signatureDataService;

    public SignatureDataController(SignatureDataService signatureDataService) {
        this.signatureDataService = signatureDataService;
    }

    @PostMapping
    public String saveAndForwardIncomingSignatureData(@RequestBody SignatureDataDTO signatureDataDto) {

        try {
            signatureDataService.saveSignatureData(signatureDataDto);
            //signatureDataService.sendSignatureDataEvent(signatureDataDto);

            log.info("Sign data saved to DB and sent to topic");
            return "Success!!";
        } catch (Exception e) {
            log.error("Could not handle incoming sign data: " + signatureDataDto.getSignatureId());
            // todo create error audit event here
            throw new RuntimeException("Error while trying to handle incoming sign data, got error: "+ e);
        }
    }
}
