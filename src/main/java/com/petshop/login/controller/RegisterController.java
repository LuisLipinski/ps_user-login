package com.petshop.login.controller;

import com.petshop.login.model.RegisterRequest;
import com.petshop.login.model.UserResponse;
import com.petshop.login.service.RegisterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private RegisterService registerService;

    @PostMapping("/newUser")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse newUser = registerService.register(registerRequest);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
