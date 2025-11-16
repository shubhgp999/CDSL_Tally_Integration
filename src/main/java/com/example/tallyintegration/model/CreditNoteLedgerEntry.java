package com.example.tallyintegration.model;

import lombok.Data;

@Data
public class CreditNoteLedgerEntry {
    private String ledger;
    private String amount;
    private String bill_ref;
    private String bill_type;
    private String bill_amount;
}
