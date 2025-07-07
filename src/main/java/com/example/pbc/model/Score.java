package com.example.pbc.model;

import java.time.LocalDateTime;

public class Score {
    private Long id;
    private Long userId;
    private String scoreNumber;
    private float balance;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    // Конструктор, геттеры
    public Score(Long id, Long userId, String scoreNumber, float balance, boolean active) {
        this.id = id;
        this.userId = userId;
        this.scoreNumber = scoreNumber;
        this.balance = balance;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }
    public Long getUserId(){
        return userId;
    }
    public String getScoreNumber(){
        return scoreNumber;
    }
    public float getBalance(){
        return balance;
    }
    public boolean getStatus(){
        return active;
    }

}
