package com.petshop.login.model;

import org.springframework.data.domain.Sort;

public enum SortField {
    NOME("nome"),
    ID("id"),
    EMAIL("email"),
    NIVEL_ACESSO("nivelAcesso");

    private String field;

    SortField(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
