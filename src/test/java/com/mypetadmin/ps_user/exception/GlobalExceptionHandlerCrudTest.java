package com.mypetadmin.ps_user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerCrudTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveMapearRequisicaoInvalidaComo400() {
        var request = new MockHttpServletRequest("GET", "/internal/usuarios");

        var response = handler.handleUserBadRequest(
                new UsuarioRequisicaoInvalidaException("Filtro inválido"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("USER_INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("Filtro inválido");
    }
}
