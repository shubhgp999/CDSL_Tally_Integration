package com.example.tallyintegration.model;

import lombok.Data;

import java.util.List;

@Data
public class Sales{
    private String company;
    private String voucher_type;
    private String voucher_no;
    private String voucher_date;
    private String voucher_date_old;
    private String party_ledger;
    private String narration;
    private List<SalesLedgerEntry> ledgers;
}