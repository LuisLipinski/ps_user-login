package com.mypetadmin.ps_user.exception;

public class PrimeiroMasterProtegidoException extends RuntimeException {
    public PrimeiroMasterProtegidoException() {
        super("O primeiro MASTER da empresa não pode ser excluído");
    }
}
