package com.example.pbc.model;

public class Transfer {
    private String fromScoreNumber;
    private String toScoreNumber;
    private Long amount;

    public Transfer(String fromScoreNumber, String toScoreNumber, Long amount) {
        this.fromScoreNumber = fromScoreNumber;
        this.toScoreNumber = toScoreNumber;
        this.amount = amount;
    }

    public String getFromScoreNumber(){
        return fromScoreNumber;
    }
    public String getToScoreNumber(){
        return toScoreNumber;
    }
    public Long getAmount(){
        return amount;
    }
}