package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> createLedger(@RequestBody String ledgerXml) {
        // ledgerXml is raw XML string from Postman
        TallyResult result = ledgerService.createLedgerFromXml(ledgerXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}
