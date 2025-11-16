package com.example.tallyintegration.service;

import com.example.tallyintegration.exception.InvalidRequestException;
import com.example.tallyintegration.exception.TallyConnectionException;
import com.example.tallyintegration.model.Ledger;
import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.util.LedgerXmlParser;
import com.example.tallyintegration.util.TallyXmlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class LedgerService {

    @Value("${tally.url:http://localhost:9000}")
    private String tallyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TallyResult createLedgerFromXml(String rawXml) {
        try {
            if (rawXml == null || rawXml.isBlank()) {
                throw new InvalidRequestException("Empty request body");
            }

            // Parse incoming XML into Ledger model
            Ledger ledger = LedgerXmlParser.parseLedger(rawXml);
            log.info("Parsed ledger request: name='{}', parent='{}'", ledger.getName(), ledger.getParent());

            // Build envelope for Tally
            String tallyEnvelope = TallyXmlBuilder.buildLedgerImportEnvelope(ledger);
            log.debug("Built Tally Envelope:\n{}", tallyEnvelope);

            // Send to Tally
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            HttpEntity<String> entity = new HttpEntity<>(tallyEnvelope, headers);

            log.info("Posting to Tally at {}", tallyUrl);
            ResponseEntity<String> responseEntity = restTemplate.exchange(tallyUrl, HttpMethod.POST, entity, String.class);

            String body = responseEntity.getBody();
            log.info("Tally returned HTTP {} and {} chars", responseEntity.getStatusCodeValue(), (body == null ? 0 : body.length()));
            log.debug("Tally response:\n{}", body);

            // Interpret Tally response simply: check for LINEERROR or ALTERED
            if (body == null) {
                throw new TallyConnectionException("Empty response from Tally");
            }

            if (body.contains("<LINEERROR>")) {
                String err = extractBetween(body, "<LINEERROR>", "</LINEERROR>");
                return TallyResult.failure("Tally returned LINEERROR: " + err);
            }

            if (body.contains("<ALTERED>")) {
                String alt = extractBetween(body, "<ALTERED>", "</ALTERED>");
                if ("1".equals(alt)) {
                    return TallyResult.success("Data altered successfully in Tally", body);
                }
            }

            // else treat as success
            return TallyResult.success("Ledger created/imported successfully", body);

        } catch (InvalidRequestException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return TallyResult.failure(e.getMessage());
        } catch (TallyConnectionException e) {
            log.error("Tally connection issue: {}", e.getMessage());
            return TallyResult.failure("Tally connection error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
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
