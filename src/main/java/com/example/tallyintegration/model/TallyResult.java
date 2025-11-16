package com.example.tallyintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TallyResult {
    private boolean success;
    private String message;
    private String rawResponse;

    public static TallyResult success(String msg, String raw) { return new TallyResult(true, msg, raw); }
    public static TallyResult success(String msg) { return new TallyResult(true, msg, null); }
    public static TallyResult failure(String err) { return new TallyResult(false, err, null); }
}
