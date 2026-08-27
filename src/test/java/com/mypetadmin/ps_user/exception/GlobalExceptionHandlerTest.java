package com.mypetadmin.ps_user.exception;

import jakarta.servlet.ServletRequestBindingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveMapearConflitosDeDominio() {
        assertResponse(handler.handleEmail(new EmailExistenteException(), request()), HttpStatus.CONFLICT, "USER_EMAIL_EXISTS");
        assertResponse(handler.handleOnboarding(new OnboardingConflictException(), request()), HttpStatus.CONFLICT, "USER_ONBOARDING_CONFLICT");
        assertResponse(handler.handlePrimaryMasterExists(new PrimaryMasterExistenteException(), request()), HttpStatus.CONFLICT, "USER_PRIMARY_MASTER_EXISTS");
        assertResponse(handler.handlePrimaryMasterProtected(new PrimeiroMasterProtegidoException(), request()), HttpStatus.CONFLICT, "USER_PRIMARY_MASTER_PROTECTED");
    }

    @Test
    void deveMapearErrosDeGestao() {
        assertResponse(handler.handleUserNotFound(new UsuarioNaoEncontradoException(), request()), HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        assertResponse(handler.handleUserForbidden(new UsuarioOperacaoNaoPermitidaException(), request()), HttpStatus.FORBIDDEN, "USER_OPERATION_FORBIDDEN");
    }

    @Test
    void deveMapearFalhasDeEmpresa() {
        assertResponse(handler.handleEmpresaNotFound(new EmpresaNaoEncontradaException(), request()), HttpStatus.UNPROCESSABLE_ENTITY, "USER_EMPRESA_NOT_FOUND");
        assertResponse(handler.handleEmpresaUnavailable(new EmpresaIndisponivelException(), request()), HttpStatus.SERVICE_UNAVAILABLE, "USER_EMPRESA_UNAVAILABLE");
    }

    @Test
    void deveMapearRequisicaoMalformada() {
        assertResponse(handler.handleBadRequest(new ServletRequestBindingException("header ausente"), request()),
                HttpStatus.BAD_REQUEST, "USER_BAD_REQUEST");
    }

    @Test
    void deveMapearErroInesperadoSemExporDetalhe() {
        var response = handler.handleUnexpected(new RuntimeException("segredo interno"), request());
        assertResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "USER_INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Erro interno");
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("POST", "/internal/usuarios/master");
    }

    private void assertResponse(org.springframework.http.ResponseEntity<ErrorResponse> response,
                                HttpStatus status,
                                String code) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(code);
        assertThat(response.getBody().path()).isEqualTo("/internal/usuarios/master");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
