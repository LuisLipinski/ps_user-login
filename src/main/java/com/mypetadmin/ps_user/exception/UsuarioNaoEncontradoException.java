package com.mypetadmin.ps_user.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado");
    }
}
