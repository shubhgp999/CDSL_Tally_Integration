package com.example.tallyintegration.service;

import com.example.tallyintegration.exception.InvalidRequestException;
import com.example.tallyintegration.exception.TallyConnectionException;
import com.example.tallyintegration.model.Sales;
import com.example.tallyintegration.model.TallyResult;
import com.example.tallyintegration.util.SalesXmlParser;
import com.example.tallyintegration.util.TallyXmlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class SalesService {
    @Value("${tally.url:http://localhost:9000}")
    private String tallyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TallyResult importSales(String rawXml){
        try{
            if (rawXml == null || rawXml.isBlank()) {
                throw new InvalidRequestException("Empty request body");
            }

            Sales sales = SalesXmlParser.parse(rawXml);
            log.info("Parsed Sales: voucher_no={}, party={}", sales.getVoucher_no(), sales.getParty_ledger());

            String envelop = TallyXmlBuilder.buildSalesEnvelope(sales);
            log.debug("Built Receipt Envelop: \n{}", envelop);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(envelop, headers);

            ResponseEntity<String> resp = restTemplate.exchange(tallyUrl, HttpMethod.POST, entity, String.class);
            String body = resp.getBody();
            log.info("Tally response status: {}, length: {}", resp.getStatusCode().value(), (body==null?0:body.length()));
            log.debug("Tally response:\n{}", body);

            if(body == null){
                throw new TallyConnectionException("Empty response from Tally");
            }
            if(body.contains("<LINEERROR>")) {
                String err = extractBetween(body, "<LINEERROR>","</LINEERROR>");
                return TallyResult.failure("Tally LINEERROR: " + err);
            }

            String altered = extractBetween(body, "<ALTERED>", "</ALTERED>");
            if("1".equals(altered)) {
                return TallyResult.success("Data altered successfully in Tally", body);
            }

            return TallyResult.success( " Sales Imported successfully", body);

        } catch (InvalidRequestException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return TallyResult.failure(e.getMessage());
        } catch (TallyConnectionException e) {
            log.error("Tally connection issue: {}", e.getMessage());
            return TallyResult.failure("Tally connection error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error importing sales", e);
            return TallyResult.failure("Internal server error: " + e.getMessage());
        }
    }

    private static String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        int e = text.indexOf(end, s + start.length());
        if(s >= 0 && e > s) return text.substring(s + start.length(), e).trim();
        return "";
    }
}