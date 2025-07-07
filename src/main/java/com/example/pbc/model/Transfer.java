package com.example.pbc.model;

public class Transfer {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Long amountKopecks;

    public Transfer(String fromAccountNumber, String toAccountNumber, Long amountKopecks) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amountKopecks = amountKopecks;
    }

    // геттеры
}