package com.example.tallyintegration.service;

import com.example.tallyintegration.exception.*;
import com.example.tallyintegration.model.*;
import com.example.tallyintegration.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JournalService {

    @Value("${tally.url:http://localhost:9000}")
    private String tallyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TallyResult importJournal(String rawXml) {
        try {
            if (rawXml == null || rawXml.isBlank()) {
                throw new InvalidRequestException("Empty request body");
            }

            Journal journal = JournalXmlParser.parse(rawXml);
            log.info("Parsed Journal: voucher_no={}, party={}", journal.getVoucher_no(), journal.getParty_ledger());

            String envelope = TallyXmlBuilder.buildJournalEnvelope(journal);
            log.debug("Built Journal Envelope: \n{}", envelope);

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
                return TallyResult.success("Journal created successfully in Tally", body);
            } else if ("1".equals(altered)) {
                return TallyResult.success("Journal altered successfully in Tally", body);
            } else {
                return TallyResult.success("Journal imported successfully", body);
            }

        } catch (InvalidRequestException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return TallyResult.failure(e.getMessage());
        } catch (TallyConnectionException e) {
            log.error("Tally connection issue: {}", e.getMessage());
            return TallyResult.failure("Tally connection error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error importing journal", e);
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
