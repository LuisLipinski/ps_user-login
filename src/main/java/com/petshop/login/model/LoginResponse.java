package com.petshop.login.model;

import java.time.LocalDateTime;

public class LoginResponse {
    private final String email;
    private final String nome;
    private final LocalDateTime criadoEm;
    private final String token;

    public LoginResponse(String email, String nome, LocalDateTime criadoEm, String token) {
        this.email = email;
        this.nome = nome;
        this.criadoEm = criadoEm;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getToken() {
        return token;
    }
}