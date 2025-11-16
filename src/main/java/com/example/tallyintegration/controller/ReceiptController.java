package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> importReceipt(@RequestBody String rawXml) {
        TallyResult result = receiptService.importReceipt(rawXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}
