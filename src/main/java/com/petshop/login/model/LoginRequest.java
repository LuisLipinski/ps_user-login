package com.petshop.login.model;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Nome não pode estar em branco")
    private String nome;

    @NotBlank(message = "Senha não pode estar em branco")
    private String senha;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}