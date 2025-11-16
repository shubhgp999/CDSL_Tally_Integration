package com.example.tallyintegration.controller;

import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TallyResult> importJournal(@RequestBody String rawXml) {
        TallyResult result = journalService.importJournal(rawXml);
        return ResponseEntity.status(result.isSuccess() ? 200 : 502).body(result);
    }
}
