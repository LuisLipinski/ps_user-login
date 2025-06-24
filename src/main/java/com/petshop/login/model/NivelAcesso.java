package com.petshop.login.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NivelAcesso {
    MASTER("Master"),
    ADMIN("Administrador"),
    VETERINARIO("Veterinario"),
    LOJA("Loja"),
    BANHO("Banho"),
    CRECHE("Creche"),
    ESTOQUE("Estoque");

    private final String descricao;

    NivelAcesso(String descricao) {
        this.descricao = descricao;
    }

    @JsonCreator
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static NivelAcesso fromValue(String value) {
        for (NivelAcesso nivel : values()) {
            if (nivel.name().equalsIgnoreCase(value)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nível de acesso inválido: " + value);
    }

    @Override
    public String toString() {
        return descricao;
    }
}