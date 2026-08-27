package com.mypetadmin.ps_user.exception;

public class PrimaryMasterExistenteException extends RuntimeException {
    public PrimaryMasterExistenteException() {
        super("A empresa já possui o primeiro MASTER cadastrado");
    }
}
