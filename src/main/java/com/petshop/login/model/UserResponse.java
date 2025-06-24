package com.petshop.login.model;

import java.time.LocalDateTime;

public class UserResponse {
    private final Long id;
    private final String nome;
    private final String email;
    private final NivelAcesso nivelAcesso;
    private final LocalDateTime criadoEm;

    public UserResponse(Long id, String nome, String email, NivelAcesso nivelAcesso, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.nivelAcesso = nivelAcesso;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

}