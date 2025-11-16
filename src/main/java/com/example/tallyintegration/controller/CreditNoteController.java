package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.CreditNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/creditnote")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> importCreditNote(@RequestBody String rawXml) {
        TallyResult result = creditNoteService.importCreditNote(rawXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}
