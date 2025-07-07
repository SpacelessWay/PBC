package com.example.pbc.model;

public class AuthResponse {
    private String uuid;
    private String token;

    public AuthResponse(String uuid, String token) {
        this.uuid = uuid;
        this.token = token;
    }

    public String getUuid() {
        return uuid;
    }

    public String getToken() {
        return token;
    }
}