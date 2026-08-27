package com.mypetadmin.ps_user.exception;

public class UsuarioOperacaoNaoPermitidaException extends RuntimeException {
    public UsuarioOperacaoNaoPermitidaException() {
        super("Usuário não possui permissão para esta operação");
    }
}
