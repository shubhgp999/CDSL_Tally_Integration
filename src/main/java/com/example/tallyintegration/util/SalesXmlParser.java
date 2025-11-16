package com.example.tallyintegration.util;

import com.example.tallyintegration.model.Sales;
import com.example.tallyintegration.model.SalesLedgerEntry;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class SalesXmlParser {

    public static Sales parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        Element root = doc.getDocumentElement();
        Sales s = new Sales();

        s.setCompany(getTagValue("company", root));
        s.setVoucher_type(getTagValue("voucher_type", root));
        s.setVoucher_no(getTagValue("voucher_no", root));
        s.setVoucher_date(getTagValue("voucher_date", root));
        s.setVoucher_date_old(getTagValue("voucher_date_old", root));
        s.setParty_ledger(getTagValue("party_ledger", root));
        s.setNarration(getTagValue("narration", root));

        NodeList ledgerNodes = root.getElementsByTagName("ledger_entry");
        List<SalesLedgerEntry> entries = new ArrayList<>();

        for (int i = 0; i < ledgerNodes.getLength(); i++) {
            Element ledgerEl = (Element) ledgerNodes.item(i);
            SalesLedgerEntry entry = new SalesLedgerEntry();

            entry.setLedger(getTagValue("ledger", ledgerEl));
            entry.setAmount(getTagValue("amount", ledgerEl));

            // ✅ critical part — add this:
            entry.setBill_ref(getTagValue("bill_ref", ledgerEl));
            entry.setBill_type(getTagValue("bill_type", ledgerEl));

            // 🧠 many people forget this line — the root cause of your bug:
            entry.setBill_amount_csv(getTagValue("bill_amount_csv", ledgerEl));

            // optional fallback
            entry.setBill_amount(getTagValue("bill_amount", ledgerEl));

            entries.add(entry);
        }

        s.setLedgers(entries);
        return s;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList list = element.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        Node node = list.item(0);
        return node.getTextContent().trim();
    }
}
