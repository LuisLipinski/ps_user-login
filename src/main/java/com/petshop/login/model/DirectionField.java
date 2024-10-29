package com.petshop.login.model;

public enum DirectionField {
    ASC("asc"),
    DESC("desc");

    private String direction;

    DirectionField(String direction){
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }
}
