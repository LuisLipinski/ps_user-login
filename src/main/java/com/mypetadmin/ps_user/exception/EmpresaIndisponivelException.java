package com.mypetadmin.ps_user.exception;

public class EmpresaIndisponivelException extends RuntimeException {
    public EmpresaIndisponivelException() {
        super("Não foi possível validar a empresa");
    }
}
