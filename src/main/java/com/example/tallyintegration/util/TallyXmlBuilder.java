package com.example.tallyintegration.util;

import com.example.tallyintegration.model.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class TallyXmlBuilder {

    // =============================================================
    //  LEDGER IMPORT
    // =============================================================
    public static String buildLedgerImportEnvelope(Ledger ledger) {
        String company = safe(ledger.getCompany(), "Default Company");
        String name = escape(ledger.getName());
        String oldName = escape(safe(ledger.getOldName(), ledger.getName()));
        String alias = escape(safe(ledger.getAlias(), ""));
        String parent = escape(safe(ledger.getParent(), ""));
        String opening = escape(safe(ledger.getOpeningBalance(), "0"));
        String address1 = escape(safe(ledger.getAddress1(), ""));
        String address2 = escape(safe(ledger.getAddress2(), ""));
        String address3 = escape(safe(ledger.getAddress3(), ""));
        String address4 = escape(safe(ledger.getAddress4(), ""));
        String pincode = escape(safe(ledger.getPincode(), ""));
        String state = escape(safe(ledger.getState(), ""));
        String country = escape(safe(ledger.getCountry(), "India"));
        String mailing = name;
        String tan = escape(safe(ledger.getTanNo(), ""));
        String gstType = escape(safe(ledger.getGstType(), ""));
        String gstNo = escape(safe(ledger.getGstNo(), ""));

        return "<ENVELOPE>" +
                "<HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>" +
                "<BODY><IMPORTDATA>" +
                "<REQUESTDESC><REPORTNAME>All Masters</REPORTNAME>" +
                "<STATICVARIABLES><SVCURRENTCOMPANY>" + escape(company) + "</SVCURRENTCOMPANY></STATICVARIABLES>" +
                "</REQUESTDESC><REQUESTDATA>" +
                "<TALLYMESSAGE xmlns:UDF=\"TallyUDF\">" +
                "<LEDGER NAME=\"" + oldName + "\" RESERVEDNAME=\"\">" +
                "<LANGUAGENAME.LIST><NAME.LIST TYPE=\"String\">" +
                "<NAME>" + name + "</NAME>" +
                "<NAME>" + alias + "</NAME>" +
                "</NAME.LIST><LANGUAGEID>1033</LANGUAGEID></LANGUAGENAME.LIST>" +
                "<PARENT>" + parent + "</PARENT>" +
                "<ISBILLWISEON>Yes</ISBILLWISEON>" +
                "<OPENINGBALANCE>" + opening + "</OPENINGBALANCE>" +
                "<LEDMAILINGDETAILS.LIST>" +
                "<ADDRESS.LIST TYPE=\"String\">" +
                "<ADDRESS>" + address1 + "</ADDRESS>" +
                "<ADDRESS>" + address2 + "</ADDRESS>" +
                "<ADDRESS>" + address3 + "</ADDRESS>" +
                "<ADDRESS>" + address4 + "</ADDRESS>" +
                "</ADDRESS.LIST>" +
                "<APPLICABLEFROM>20240401</APPLICABLEFROM>" +
                "<PINCODE>" + pincode + "</PINCODE>" +
                "<STATE>" + state + "</STATE>" +
                "<COUNTRY>" + country + "</COUNTRY>" +
                "<MAILINGNAME>" + mailing + "</MAILINGNAME>" +
                "</LEDMAILINGDETAILS.LIST>" +
                "<COUNTRYOFRESIDENCE>" + country + "</COUNTRYOFRESIDENCE>" +
                "<LEDGERMOBILE/>" +
                "<TANNumber>" + tan + "</TANNumber>" +
                "<LEDGSTREGDETAILS.LIST>" +
                "<APPLICABLEFROM>20240401</APPLICABLEFROM>" +
                "<GSTREGISTRATIONTYPE>" + gstType + "</GSTREGISTRATIONTYPE>" +
                "<PLACEOFSUPPLY>" + state + "</PLACEOFSUPPLY>" +
                "<GSTIN>" + gstNo + "</GSTIN>" +
                "</LEDGSTREGDETAILS.LIST>" +
                "</LEDGER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>";
    }

    // =============================================================
    //  RECEIPT
    // =============================================================
    public static String buildReceiptEnvelope(Receipt r) {
        String company = safe(r.getCompany(), "Default Company");
        String remoteId = safe(r.getVoucher_no(), "") + safe(r.getVoucher_type(), "")
                + safe(r.getVoucher_date(), "") + safe(r.getParty_ledger(), "");

        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>")
                .append(escape(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">");

        sb.append("<VOUCHER REMOTEID=\"").append(escape(remoteId))
                .append("\" VCHTYPE=\"").append(escape(safe(r.getVoucher_type(), "")))
                .append("\" ACTION=\"Create\" OBJVIEW=\"Accounting Voucher View\">");
        sb.append("<DATE>").append(escape(safe(r.getVoucher_date(), ""))).append("</DATE>");
        sb.append("<VOUCHERTYPENAME>").append(escape(safe(r.getVoucher_type(), ""))).append("</VOUCHERTYPENAME>");
        sb.append("<VOUCHERNUMBER>").append(escape(safe(r.getVoucher_no(), ""))).append("</VOUCHERNUMBER>");
        sb.append("<PARTYLEDGERNAME>").append(escape(safe(r.getParty_ledger(), ""))).append("</PARTYLEDGERNAME>");
        sb.append("<NARRATION>").append(escape(safe(r.getNarration(), ""))).append("</NARRATION>");
        sb.append("<PERSISTEDVIEW>Accounting Voucher View</PERSISTEDVIEW>");
        sb.append("<ISOPTIONAL>No</ISOPTIONAL>");

        List<ReceiptLedgerEntry> entries = r.getLedgers();
        if (entries != null) {
            for (ReceiptLedgerEntry e : entries) {
                String ledgerName = safe(e.getLedger(), "");
                boolean isParty = ledgerName.equals(r.getParty_ledger());
                String isDeemedPositive = isParty ? "No" : "Yes";
                double amt = parseDoubleSafe(e.getAmount(), 0.0);
                double voucherAmount = isParty ? amt : -amt;

                sb.append("<ALLLEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(escape(ledgerName)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isDeemedPositive).append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(formatAmount(voucherAmount)).append("</AMOUNT>");

                if (!safe(e.getBill_ref(), "").isEmpty()) {
                    double billAmt = parseDoubleSafe(e.getBill_amount(), 0.0);
                    double billAmount = isParty ? billAmt : -billAmt;
                    sb.append("<BILLALLOCATIONS.LIST>");
                    sb.append("<NAME>").append(escape(e.getBill_ref())).append("</NAME>");
                    sb.append("<BILLTYPE>").append(escape(safe(e.getBill_type(), "New Ref"))).append("</BILLTYPE>");
                    sb.append("<AMOUNT>").append(formatAmount(billAmount)).append("</AMOUNT>");
                    sb.append("</BILLALLOCATIONS.LIST>");
                }

                sb.append("</ALLLEDGERENTRIES.LIST>");
            }
        }

        sb.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        return sb.toString();
    }

    // =============================================================
    //  SALES  (auto New Ref bill allocation fix)
    // =============================================================
    public static String buildSalesEnvelope(Sales s) {
        String company = safe(s.getCompany(), "Default Company");
        String remoteId = safe(s.getVoucher_no(), "")
                + safe(s.getVoucher_type(), "")
                + safe(s.getVoucher_date(), "")
                + safe(s.getParty_ledger(), "");

        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>")
                .append(escape(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">");

        sb.append("<VOUCHER REMOTEID=\"").append(escape(remoteId))
                .append("\" VCHTYPE=\"").append(escape(safe(s.getVoucher_type(), "")))
                .append("\" ACTION=\"Create\" OBJVIEW=\"Invoice Voucher View\">");

        sb.append("<DATE>").append(escape(safe(s.getVoucher_date(), ""))).append("</DATE>");
        sb.append("<PERSISTEDVIEW>Invoice Voucher View</PERSISTEDVIEW>");
        sb.append("<ISINVOICE>Yes</ISINVOICE>");
        sb.append("<VOUCHERNUMBER>").append(escape(safe(s.getVoucher_no(), ""))).append("</VOUCHERNUMBER>");
        sb.append("<VOUCHERTYPENAME>").append(escape(safe(s.getVoucher_type(), ""))).append("</VOUCHERTYPENAME>");
        sb.append("<PARTYLEDGERNAME>").append(escape(safe(s.getParty_ledger(), ""))).append("</PARTYLEDGERNAME>");
        sb.append("<GUID>").append(escape(remoteId)).append("</GUID>");
        sb.append("<VCHENTRYMODE>Accounting Invoice</VCHENTRYMODE>");
        sb.append("<NARRATION>").append(escape(safe(s.getNarration(), ""))).append("</NARRATION>");

        List<SalesLedgerEntry> entries = s.getLedgers();
        if (entries != null && !entries.isEmpty()) {
            for (SalesLedgerEntry e : entries) {
                String ledgerName = safe(e.getLedger(), "");
                boolean isParty = ledgerName.equalsIgnoreCase(s.getParty_ledger());
                String isDeemedPositive = isParty ? "Yes" : "No";
                double amt = parseDoubleSafe(e.getAmount(), 0.0);
                double voucherAmount = isParty ? -amt : amt;

                sb.append("<LEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(escape(ledgerName)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isDeemedPositive).append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(formatAmount(voucherAmount)).append("</AMOUNT>");
                sb.append("<ISPARTYLEDGER>").append(isParty ? "Yes" : "No").append("</ISPARTYLEDGER>");
                if (isParty) sb.append("<USEDINBILLALLOCATIONS>Yes</USEDINBILLALLOCATIONS>");
                sb.append("<USEFORGAINLOSS>No</USEFORGAINLOSS>");

                // --- Bill Allocations (Final Fix) ---
                if (isParty) {
                    String refs = safe(e.getBill_ref(), "");
                    String types = safe(e.getBill_type(), "");
                    String amounts = safe(
                            e.getBill_amount_csv() != null ? e.getBill_amount_csv() : e.getBill_amount(), "");

                    // Trim all
                    refs = refs.trim();
                    types = types.trim();
                    amounts = amounts.trim();

                    if (!refs.isEmpty() && !types.isEmpty() && !amounts.isEmpty()) {
                        String[] refArr = refs.split("~");
                        String[] typeArr = types.split("~");
                        String[] amtArr = amounts.split("~");
                        int maxLen = Math.max(refArr.length, Math.max(typeArr.length, amtArr.length));
                        double totalBillAmt = 0.0;

                        for (int i = 0; i < maxLen; i++) {
                            String ref = safeArrayValue(refArr, i);
                            String type = safeArrayValue(typeArr, i);
                            String amtStr = safeArrayValue(amtArr, i);
                            if (ref.isEmpty() && type.isEmpty()) continue;

                            double billAmt = parseDoubleSafe(amtStr, 0.0);
                            totalBillAmt += billAmt;

                            // 🔥 Use ref as-is; if New Ref -> use that, not voucher_no
                            sb.append("<BILLALLOCATIONS.LIST>");
                            sb.append("<NAME>").append(escape(ref)).append("</NAME>");
                            sb.append("<BILLTYPE>").append(escape(type)).append("</BILLTYPE>");
                            sb.append("<AMOUNT>").append(formatAmount(-billAmt)).append("</AMOUNT>");
                            sb.append("</BILLALLOCATIONS.LIST>");
                        }

                        // ✅ No “auto New Ref” if total matches
                        double diff = Math.abs(totalBillAmt - Math.abs(amt));
                        if (diff > 0.01) {
                            sb.append("<BILLALLOCATIONS.LIST>");
                            sb.append("<NAME>").append(escape(s.getVoucher_no())).append("</NAME>");
                            sb.append("<BILLTYPE>New Ref</BILLTYPE>");
                            sb.append("<AMOUNT>").append(formatAmount(-diff)).append("</AMOUNT>");
                            sb.append("</BILLALLOCATIONS.LIST>");
                        }
                    } else {
                        // If no bill details at all, create one New Ref
                        sb.append("<BILLALLOCATIONS.LIST>");
                        sb.append("<NAME>").append(escape(s.getVoucher_no())).append("</NAME>");
                        sb.append("<BILLTYPE>New Ref</BILLTYPE>");
                        sb.append("<AMOUNT>").append(formatAmount(-Math.abs(amt))).append("</AMOUNT>");
                        sb.append("</BILLALLOCATIONS.LIST>");
                    }
                }

                sb.append("</LEDGERENTRIES.LIST>");
            }
        }

        sb.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        log.debug("✅ Final Sales Envelope XML:\n{}", sb);
        return sb.toString();
    }

    // =============================================================
    //  CREDIT NOTE
    // =============================================================
    public static String buildCreditNoteEnvelope(CreditNote note) {
        String company = safe(note.getCompany(), "Default Company");
        String remoteId = safe(note.getVoucher_no(), "") +
                safe(note.getVoucher_type(), "") +
                safe(note.getVoucher_date(), "") +
                safe(note.getParty_ledger(), "");

        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>")
                .append(escape(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">");

        sb.append("<VOUCHER REMOTEID=\"").append(escape(remoteId))
                .append("\" VCHTYPE=\"").append(escape(safe(note.getVoucher_type(), "")))
                .append("\" ACTION=\"Create\" OBJVIEW=\"Accounting Voucher View\">");

        sb.append("<DATE>").append(escape(safe(note.getVoucher_date(), ""))).append("</DATE>");
        sb.append("<VOUCHERNUMBER>").append(escape(safe(note.getVoucher_no(), ""))).append("</VOUCHERNUMBER>");
        sb.append("<VOUCHERTYPENAME>").append(escape(safe(note.getVoucher_type(), ""))).append("</VOUCHERTYPENAME>");
        sb.append("<PARTYLEDGERNAME>").append(escape(safe(note.getParty_ledger(), ""))).append("</PARTYLEDGERNAME>");
        sb.append("<NARRATION>").append(escape(safe(note.getNarration(), ""))).append("</NARRATION>");
        sb.append("<PERSISTEDVIEW>Accounting Voucher View</PERSISTEDVIEW>");

        List<CreditNoteLedgerEntry> entries = note.getLedgers();
        if (entries != null) {
            for (CreditNoteLedgerEntry e : entries) {
                String ledgerName = safe(e.getLedger(), "");
                boolean isParty = ledgerName.equalsIgnoreCase(note.getParty_ledger());
                String isDeemedPositive = isParty ? "Yes" : "No";
                double amt = parseDoubleSafe(e.getAmount(), 0.0);
                double signedAmt = isParty ? -amt : amt;

                sb.append("<ALLLEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(escape(ledgerName)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isDeemedPositive).append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(formatAmount(signedAmt)).append("</AMOUNT>");
                sb.append("<ISPARTYLEDGER>").append(isParty ? "Yes" : "No").append("</ISPARTYLEDGER>");

                // Bill allocation (if applicable)
                if (isParty && e.getBill_ref() != null && !e.getBill_ref().isBlank()) {
                    sb.append("<BILLALLOCATIONS.LIST>");
                    sb.append("<NAME>").append(escape(safe(e.getBill_ref(), note.getVoucher_no()))).append("</NAME>");
                    sb.append("<BILLTYPE>").append(escape(safe(e.getBill_type(), "New Ref"))).append("</BILLTYPE>");
                    sb.append("<AMOUNT>").append(formatAmount(signedAmt)).append("</AMOUNT>");
                    sb.append("</BILLALLOCATIONS.LIST>");
                }

                sb.append("</ALLLEDGERENTRIES.LIST>");
            }
        }

        sb.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        log.debug("✅ Final Credit Note Envelope XML:\n{}", sb);
        return sb.toString();
    }

    // =============================================================
    //  DEBIT NOTE
    // =============================================================
    public static String buildDebitNoteEnvelope(DebitNote note) {
        String company = safe(note.getCompany(), "Default Company");
        String remoteId = safe(note.getVoucher_no(), "") +
                safe(note.getVoucher_type(), "") +
                safe(note.getVoucher_date(), "") +
                safe(note.getParty_ledger(), "");

        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>")
                .append(escape(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">");

        sb.append("<VOUCHER REMOTEID=\"").append(escape(remoteId))
                .append("\" VCHTYPE=\"").append(escape(safe(note.getVoucher_type(), "")))
                .append("\" ACTION=\"Create\" OBJVIEW=\"Accounting Voucher View\">");

        sb.append("<DATE>").append(escape(safe(note.getVoucher_date(), ""))).append("</DATE>");
        sb.append("<VOUCHERNUMBER>").append(escape(safe(note.getVoucher_no(), ""))).append("</VOUCHERNUMBER>");
        sb.append("<VOUCHERTYPENAME>").append(escape(safe(note.getVoucher_type(), ""))).append("</VOUCHERTYPENAME>");
        sb.append("<PARTYLEDGERNAME>").append(escape(safe(note.getParty_ledger(), ""))).append("</PARTYLEDGERNAME>");
        sb.append("<NARRATION>").append(escape(safe(note.getNarration(), ""))).append("</NARRATION>");
        sb.append("<PERSISTEDVIEW>Accounting Voucher View</PERSISTEDVIEW>");

        List<DebitNoteLedgerEntry> entries = note.getLedgers();
        if (entries != null) {
            for (DebitNoteLedgerEntry e : entries) {
                String ledgerName = safe(e.getLedger(), "");
                boolean isParty = ledgerName.equalsIgnoreCase(note.getParty_ledger());
                String isDeemedPositive = isParty ? "No" : "Yes";  // opposite logic of credit note
                double amt = parseDoubleSafe(e.getAmount(), 0.0);
                double signedAmt = isParty ? amt : -amt;

                sb.append("<ALLLEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(escape(ledgerName)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isDeemedPositive).append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(formatAmount(signedAmt)).append("</AMOUNT>");
                sb.append("<ISPARTYLEDGER>").append(isParty ? "Yes" : "No").append("</ISPARTYLEDGER>");

                if (isParty && e.getBill_ref() != null && !e.getBill_ref().isBlank()) {
                    sb.append("<BILLALLOCATIONS.LIST>");
                    sb.append("<NAME>").append(escape(safe(e.getBill_ref(), note.getVoucher_no()))).append("</NAME>");
                    sb.append("<BILLTYPE>").append(escape(safe(e.getBill_type(), "New Ref"))).append("</BILLTYPE>");
                    sb.append("<AMOUNT>").append(formatAmount(signedAmt)).append("</AMOUNT>");
                    sb.append("</BILLALLOCATIONS.LIST>");
                }

                sb.append("</ALLLEDGERENTRIES.LIST>");
            }
        }

        sb.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        log.debug("✅ Final Debit Note Envelope XML:\n{}", sb);
        return sb.toString();
    }

    // ===============================================
    // Journal
    //=================================================

    public static String buildJournalEnvelope(Journal j) {
        String company = safe(j.getCompany(), "Default Company");
        String remoteId = safe(j.getVoucher_no(), "") + safe(j.getVoucher_type(), "")
                + safe(j.getVoucher_date(), "") + safe(j.getParty_ledger(), "");

        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>")
                .append(escape(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">");

        sb.append("<VOUCHER REMOTEID=\"").append(escape(remoteId))
                .append("\" VCHTYPE=\"").append(escape(safe(j.getVoucher_type(), "")))
                .append("\" ACTION=\"Create\" OBJVIEW=\"Accounting Voucher View\">");

        sb.append("<DATE>").append(escape(safe(j.getVoucher_date(), ""))).append("</DATE>");
        sb.append("<VOUCHERNUMBER>").append(escape(safe(j.getVoucher_no(), ""))).append("</VOUCHERNUMBER>");
        sb.append("<VOUCHERTYPENAME>").append(escape(safe(j.getVoucher_type(), ""))).append("</VOUCHERTYPENAME>");
        sb.append("<PARTYLEDGERNAME>").append(escape(safe(j.getParty_ledger(), ""))).append("</PARTYLEDGERNAME>");
        sb.append("<NARRATION>").append(escape(safe(j.getNarration(), ""))).append("</NARRATION>");
        sb.append("<PERSISTEDVIEW>Accounting Voucher View</PERSISTEDVIEW>");

        List<JournalLedgerEntry> entries = j.getLedgers();
        if (entries != null && !entries.isEmpty()) {
            for (JournalLedgerEntry e : entries) {
                String ledgerName = safe(e.getLedger(), "");
                double amt = parseDoubleSafe(e.getAmount(), 0.0);

                // Logic for debit/credit split
                boolean isParty = ledgerName.equalsIgnoreCase(j.getParty_ledger());
                String isDeemedPositive = isParty ? "No" : "Yes";
                double voucherAmt = isParty ? amt : -amt;

                sb.append("<ALLLEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(escape(ledgerName)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isDeemedPositive).append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(formatAmount(voucherAmt)).append("</AMOUNT>");
                sb.append("<ISPARTYLEDGER>").append(isParty ? "Yes" : "No").append("</ISPARTYLEDGER>");

                // Bill allocations if present
                if (e.getBill_ref() != null && !e.getBill_ref().isBlank()) {
                    sb.append("<BILLALLOCATIONS.LIST>");
                    sb.append("<NAME>").append(escape(e.getBill_ref())).append("</NAME>");
                    sb.append("<BILLTYPE>").append(escape(safe(e.getBill_type(), "New Ref"))).append("</BILLTYPE>");
                    sb.append("<AMOUNT>").append(formatAmount(voucherAmt)).append("</AMOUNT>");
                    sb.append("</BILLALLOCATIONS.LIST>");
                }

                sb.append("</ALLLEDGERENTRIES.LIST>");
            }
        }

        sb.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        log.debug("✅ Final Journal Envelope XML:\n{}", sb);
        return sb.toString();
    }





    // =============================================================
    //  HELPERS
    // =============================================================
    private static double parseDoubleSafe(String s, double d) {
        try { if (s == null || s.isBlank()) return d; return Double.parseDouble(s.replaceAll(",", "")); }
        catch (Exception ex) { return d; }
    }
    private static String formatAmount(double v) { return String.format("%.2f", v); }
    private static String safe(String s, String def) { return (s == null) ? def : s; }
    private static String safeArrayValue(String[] arr, int index) {
        if (arr == null || index >= arr.length || arr[index] == null) return "";
        return arr[index].trim();
    }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
