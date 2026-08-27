package com.mypetadmin.ps_user.exception;

public class EmailExistenteException extends RuntimeException {
    public EmailExistenteException() {
        super("E-mail já cadastrado");
    }
}
