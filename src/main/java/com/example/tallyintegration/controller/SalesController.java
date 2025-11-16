package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController{

    private final SalesService salesService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> importSales(@RequestBody String rawXml){
        TallyResult result = salesService.importSales(rawXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}