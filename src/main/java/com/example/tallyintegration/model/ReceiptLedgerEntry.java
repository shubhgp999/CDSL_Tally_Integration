package com.example.tallyintegration.model;

import lombok.Data;

@Data
public class ReceiptLedgerEntry {
    private String ledger;
    private String amount;
    private String bill_ref;
    private String bill_type;
    private String bill_amount;
    // bank allocation fields
    private String bank_name;
    private String instrument_no;
    private String instrument_date;
    private String transaction_type;
    private String unique_reference_number;
}
