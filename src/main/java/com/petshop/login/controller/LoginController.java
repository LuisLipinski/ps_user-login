package com.petshop.login.controller;

import com.petshop.login.model.LoginRequest;
import com.petshop.login.model.LoginResponse;
import com.petshop.login.exception.ErrorResponse;
import com.petshop.login.exception.InvalidCredentialsException;
import com.petshop.login.exception.ValidationException;
import com.petshop.login.service.LoginService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = loginService.login(loginRequest);
        return ResponseEntity.ok(response);

    }
}