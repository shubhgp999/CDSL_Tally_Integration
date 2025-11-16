package com.example.tallyintegration.util;

import com.example.tallyintegration.model.CreditNote;
import com.example.tallyintegration.model.CreditNoteLedgerEntry;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class CreditNoteXmlParser {

    public static CreditNote parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        Element root = doc.getDocumentElement();
        CreditNote note = new CreditNote();

        note.setCompany(getTagValue("company", root));
        note.setVoucher_type(getTagValue("voucher_type", root));
        note.setVoucher_no(getTagValue("voucher_no", root));
        note.setVoucher_date(getTagValue("voucher_date", root));
        note.setVoucher_date_old(getTagValue("voucher_date_old", root));
        note.setParty_ledger(getTagValue("party_ledger", root));
        note.setNarration(getTagValue("narration", root));

        NodeList ledgerNodes = root.getElementsByTagName("ledger_entry");
        List<CreditNoteLedgerEntry> entries = new ArrayList<>();

        for (int i = 0; i < ledgerNodes.getLength(); i++) {
            Element ledgerEl = (Element) ledgerNodes.item(i);
            CreditNoteLedgerEntry entry = new CreditNoteLedgerEntry();

            entry.setLedger(getTagValue("ledger", ledgerEl));
            entry.setAmount(getTagValue("amount", ledgerEl));
            entry.setBill_ref(getTagValue("bill_ref", ledgerEl));
            entry.setBill_type(getTagValue("bill_type", ledgerEl));
            entry.setBill_amount(getTagValue("bill_amount", ledgerEl));

            entries.add(entry);
        }

        note.setLedgers(entries);
        return note;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList list = element.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }
}
