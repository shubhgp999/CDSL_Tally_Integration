package com.example.tallyintegration.util;

import com.example.tallyintegration.model.Ledger;
import com.example.tallyintegration.exception.InvalidRequestException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class LedgerXmlParser {

    public static Ledger parseLedger(String xml) {
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
            if (!"ledger".equalsIgnoreCase(root.getNodeName())) {
                throw new InvalidRequestException("Root element must be <ledger>");
            }

            Ledger ledger = new Ledger();
            ledger.setName(getTextContent(root, "name"));
            ledger.setOldName(getTextContent(root, "old_name"));
            ledger.setCompany(getTextContent(root, "company"));
            ledger.setAlias(getTextContent(root, "alias"));
            ledger.setParent(getTextContent(root, "parent"));
            ledger.setAddress1(getTextContent(root, "address1"));
            ledger.setAddress2(getTextContent(root, "address2"));
            ledger.setAddress3(getTextContent(root, "address3"));
            ledger.setAddress4(getTextContent(root, "address4"));
            ledger.setAddress5(getTextContent(root, "address5"));
            ledger.setPincode(getTextContent(root, "pincode"));
            ledger.setState(getTextContent(root, "state"));
            ledger.setCountry(getTextContent(root, "country"));
            ledger.setGstNo(getTextContent(root, "gst_no"));
            ledger.setGstType(getTextContent(root, "gst_type"));
            ledger.setOpeningBalance(getTextContent(root, "opening_balance"));
            ledger.setTanNo(getTextContent(root, "tan_no"));

            if (ledger.getName() == null || ledger.getName().isBlank()) {
                throw new InvalidRequestException("ledger.name is required");
            }

            return ledger;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to parse XML: " + e.getMessage());
        }
    }

    private static String getTextContent(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list == null || list.getLength() == 0) return null;
        Node n = list.item(0);
        if (n == null) return null;
        String text = n.getTextContent();
        return text != null ? text.trim() : null;
    }
}
