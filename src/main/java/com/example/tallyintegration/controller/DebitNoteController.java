package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.DebitNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debitnote")
@RequiredArgsConstructor
public class DebitNoteController {

    private final DebitNoteService debitNoteService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> importDebitNote(@RequestBody String rawXml) {
        TallyResult result = debitNoteService.importDebitNote(rawXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}
