package com.example.tallyintegration.util;

import com.example.tallyintegration.exception.InvalidRequestException;
import com.example.tallyintegration.model.Receipt;
import com.example.tallyintegration.model.ReceiptLedgerEntry;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReceiptXmlParser {

    public static Receipt parse(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            if (!"voucher".equalsIgnoreCase(root.getNodeName())) {
                throw new InvalidRequestException("Root element must be <voucher>");
            }

            Receipt r = new Receipt();
            r.setCompany(getText(root, "company"));
            r.setVoucher_type(getText(root, "voucher_type"));
            r.setVoucher_no(getText(root, "voucher_no"));
            r.setVoucher_date(getText(root, "voucher_date"));
            r.setVoucher_date_old(getText(root, "voucher_date_old"));
            r.setParty_ledger(getText(root, "party_ledger"));
            r.setNarration(getText(root, "narration"));

            // parse ledgers
            NodeList ledgersNodes = root.getElementsByTagName("ledgers");
            List<ReceiptLedgerEntry> ledgers = new ArrayList<>();
            if (ledgersNodes != null && ledgersNodes.getLength() > 0) {
                Element ledgersElem = (Element) ledgersNodes.item(0);
                NodeList entries = ledgersElem.getElementsByTagName("ledger_entry");
                for (int i = 0; i < entries.getLength(); i++) {
                    Node node = entries.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element le = (Element) node;
                    ReceiptLedgerEntry entry = new ReceiptLedgerEntry();
                    entry.setLedger(getText(le, "ledger"));
                    entry.setAmount(getText(le, "amount"));
                    entry.setBill_ref(getText(le, "bill_ref"));
                    entry.setBill_type(getText(le, "bill_type"));
                    entry.setBill_amount(getText(le, "bill_amount"));
                    entry.setBank_name(getText(le, "bank_name"));
                    entry.setInstrument_no(getText(le, "instrument_no"));
                    entry.setInstrument_date(getText(le, "instrument_date"));
                    entry.setTransaction_type(getText(le, "transaction_type"));
                    entry.setUnique_reference_number(getText(le, "unique_reference_number"));
                    ledgers.add(entry);
                }
            }
            r.setLedgers(ledgers);

            if (r.getParty_ledger() == null || r.getParty_ledger().isBlank()) {
                throw new InvalidRequestException("party_ledger is required");
            }
            return r;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to parse voucher XML: " + e.getMessage());
        }
    }

    private static String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list == null || list.getLength() == 0) return null;
        Node n = list.item(0);
        if (n == null) return null;
        String txt = n.getTextContent();
        return txt == null ? null : txt.trim();
    }
}
