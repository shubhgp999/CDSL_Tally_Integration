package com.example.tallyintegration.util;

import com.example.tallyintegration.model.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class DebitNoteXmlParser {

    public static DebitNote parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
        doc.getDocumentElement().normalize();

        DebitNote note = new DebitNote();
        note.setCompany(getTag(doc, "company"));
        note.setVoucher_type(getTag(doc, "voucher_type"));
        note.setVoucher_no(getTag(doc, "voucher_no"));
        note.setVoucher_date(getTag(doc, "voucher_date"));
        note.setVoucher_date_old(getTag(doc, "voucher_date_old"));
        note.setParty_ledger(getTag(doc, "party_ledger"));
        note.setNarration(getTag(doc, "narration"));

        List<DebitNoteLedgerEntry> entries = new ArrayList<>();
        NodeList ledgerNodes = doc.getElementsByTagName("ledger_entry");
        for (int i = 0; i < ledgerNodes.getLength(); i++) {
            Node n = ledgerNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                DebitNoteLedgerEntry le = new DebitNoteLedgerEntry();
                le.setLedger(getTag(e, "ledger"));
                le.setAmount(getTag(e, "amount"));
                le.setBill_ref(getTag(e, "bill_ref"));
                le.setBill_type(getTag(e, "bill_type"));
                le.setBill_amount(getTag(e, "bill_amount"));
                entries.add(le);
            }
        }
        note.setLedgers(entries);
        return note;
    }

    private static String getTag(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagName(tag);
        return (nl.getLength() > 0) ? nl.item(0).getTextContent().trim() : "";
    }

    private static String getTag(Element e, String tag) {
        NodeList nl = e.getElementsByTagName(tag);
        return (nl.getLength() > 0) ? nl.item(0).getTextContent().trim() : "";
    }
}
