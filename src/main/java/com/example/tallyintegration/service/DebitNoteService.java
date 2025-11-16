package com.example.tallyintegration.service;

import com.example.tallyintegration.exception.InvalidRequestException;
import com.example.tallyintegration.exception.TallyConnectionException;
import com.example.tallyintegration.model.DebitNote;
import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.util.DebitNoteXmlParser;
import com.example.tallyintegration.util.TallyXmlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DebitNoteService {

    @Value("${tally.url:http://localhost:9000}")
    private String tallyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TallyResult importDebitNote(String rawXml) {
        try {
            if (rawXml == null || rawXml.isBlank()) {
                throw new InvalidRequestException("Empty request body");
            }

            DebitNote note = DebitNoteXmlParser.parse(rawXml);
            log.info("Parsed Debit Note: voucher_no={}, party={}", note.getVoucher_no(), note.getParty_ledger());

            String envelope = TallyXmlBuilder.buildDebitNoteEnvelope(note);
            log.debug("Built Debit Note Envelope: \n{}", envelope);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(envelope, headers);

            ResponseEntity<String> resp = restTemplate.exchange(tallyUrl, HttpMethod.POST, entity, String.class);
            String body = resp.getBody();
            log.info("Tally response status: {}, length: {}", resp.getStatusCode().value(), (body == null ? 0 : body.length()));
            log.debug("Tally response:\n{}", body);

            if (body == null) {
                throw new TallyConnectionException("Empty response from Tally");
            }
            if (body.contains("<LINEERROR>")) {
                String err = extractBetween(body, "<LINEERROR>", "</LINEERROR>");
                return TallyResult.failure("Tally LINEERROR: " + err);
            }

            String created = extractBetween(body, "<CREATED>", "</CREATED>");
            String altered = extractBetween(body, "<ALTERED>", "</ALTERED>");
            if ("1".equals(created)) {
                return TallyResult.success("Debit Note created successfully in Tally", body);
            } else if ("1".equals(altered)) {
                return TallyResult.success("Debit Note altered successfully in Tally", body);
            } else {
                return TallyResult.success("Debit Note imported successfully", body);
            }

        } catch (InvalidRequestException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return TallyResult.failure(e.getMessage());
        } catch (TallyConnectionException e) {
            log.error("Tally connection issue: {}", e.getMessage());
            return TallyResult.failure("Tally connection error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error importing debit note", e);
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
