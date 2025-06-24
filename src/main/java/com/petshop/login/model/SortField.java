package com.petshop.login.model;

public enum SortField {
    NOME("nome"),
    ID("id"),
    EMAIL("email"),
    NIVEL_ACESSO("nivelAcesso");

    private final String field;

    SortField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}