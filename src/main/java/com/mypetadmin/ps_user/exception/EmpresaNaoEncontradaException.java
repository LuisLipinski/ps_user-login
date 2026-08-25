package com.mypetadmin.ps_user.exception;

public class EmpresaNaoEncontradaException extends RuntimeException {
    public EmpresaNaoEncontradaException() {
        super("Empresa não encontrada");
    }
}
