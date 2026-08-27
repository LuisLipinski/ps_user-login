package com.mypetadmin.ps_user.exception;

import jakarta.servlet.ServletRequestBindingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailExistenteException.class)
    ResponseEntity<ErrorResponse> handleEmail(EmailExistenteException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "USER_EMAIL_EXISTS", ex, request);
    }

    @ExceptionHandler(OnboardingConflictException.class)
    ResponseEntity<ErrorResponse> handleOnboarding(OnboardingConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "USER_ONBOARDING_CONFLICT", ex, request);
    }

    @ExceptionHandler(PrimaryMasterExistenteException.class)
    ResponseEntity<ErrorResponse> handlePrimaryMasterExists(PrimaryMasterExistenteException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "USER_PRIMARY_MASTER_EXISTS", ex, request);
    }

    @ExceptionHandler(PrimeiroMasterProtegidoException.class)
    ResponseEntity<ErrorResponse> handlePrimaryMasterProtected(PrimeiroMasterProtegidoException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "USER_PRIMARY_MASTER_PROTECTED", ex, request);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    ResponseEntity<ErrorResponse> handleUserNotFound(UsuarioNaoEncontradoException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex, request);
    }

    @ExceptionHandler(UsuarioOperacaoNaoPermitidaException.class)
    ResponseEntity<ErrorResponse> handleUserForbidden(UsuarioOperacaoNaoPermitidaException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "USER_OPERATION_FORBIDDEN", ex, request);
    }

    @ExceptionHandler(EmpresaNaoEncontradaException.class)
    ResponseEntity<ErrorResponse> handleEmpresaNotFound(EmpresaNaoEncontradaException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "USER_EMPRESA_NOT_FOUND", ex, request);
    }

    @ExceptionHandler(EmpresaIndisponivelException.class)
    ResponseEntity<ErrorResponse> handleEmpresaUnavailable(EmpresaIndisponivelException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "USER_EMPRESA_UNAVAILABLE", ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("request.validation_failed method={} path={} fields={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField()).distinct().toList());
        return response(HttpStatus.BAD_REQUEST, "USER_VALIDATION_ERROR", "Dados inválidos", request);
    }

    @ExceptionHandler({ServletRequestBindingException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("request.bad_request method={} path={} type={}",
                request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName());
        return response(HttpStatus.BAD_REQUEST, "USER_BAD_REQUEST", "Requisição inválida", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("request.unexpected_error method={} path={}", request.getMethod(), request.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "USER_INTERNAL_ERROR", "Erro interno", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, RuntimeException ex, HttpServletRequest request) {
        log.warn("request.rejected code={} method={} path={}", code, request.getMethod(), request.getRequestURI());
        return response(status, code, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        ));
    }
}
