package com.petshop.login.model;

import java.time.LocalDateTime;

public class LoginResponse {
    private String email;
    private String nome;
    private LocalDateTime criadoEm;
    private String token;

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
