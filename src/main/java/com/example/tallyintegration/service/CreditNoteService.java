package com.example.tallyintegration.service;

import com.example.tallyintegration.exception.InvalidRequestException;
import com.example.tallyintegration.exception.TallyConnectionException;
import com.example.tallyintegration.model.CreditNote;
import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.util.CreditNoteXmlParser;
import com.example.tallyintegration.util.TallyXmlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CreditNoteService {

    @Value("${tally.url:http://localhost:9000}")
    private String tallyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TallyResult importCreditNote(String rawXml) {
        try {
            if (rawXml == null || rawXml.isBlank()) {
                throw new InvalidRequestException("Empty request body");
            }

            CreditNote note = CreditNoteXmlParser.parse(rawXml);
            log.info("Parsed Credit Note: voucher_no={}, party={}", note.getVoucher_no(), note.getParty_ledger());

            String envelop = TallyXmlBuilder.buildCreditNoteEnvelope(note);
            log.debug("Built Credit Note Envelope: \n{}", envelop);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(envelop, headers);

            ResponseEntity<String> resp = restTemplate.exchange(tallyUrl, HttpMethod.POST, entity, String.class);
            String body = resp.getBody();
            log.info("Tally response status: {}, length: {}", resp.getStatusCode().value(), (body == null ? 0 : body.length()));
            log.debug("Tally response:\n{}", body);

            if (body == null) throw new TallyConnectionException("Empty response from Tally");

            if (body.contains("<LINEERROR>")) {
                String err = extractBetween(body, "<LINEERROR>", "</LINEERROR>");
                return TallyResult.failure("Tally LINEERROR: " + err);
            }

            return TallyResult.success("Credit Note Imported Successfully", body);

        } catch (Exception e) {
            log.error("Error importing credit note", e);
            return TallyResult.failure("Internal server error: " + e.getMessage());
        }
    }

    private static String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        int e = text.indexOf(end, s + start.length());
        if (s >= 0 && e > s) return text.substring(s + start.length(), e).trim();
        return "";
    }
}
