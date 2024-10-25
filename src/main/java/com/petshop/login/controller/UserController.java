package com.petshop.login.controller;

import com.petshop.login.model.*;
import com.petshop.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>>getAllUsers(@RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                         @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
        try {
            List<UserResponse> users = userService.getAllUsersSorted(sortField, direction);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/name")
    public ResponseEntity<UserResponse> getByName(@RequestParam String nome) {
        try {
            UserResponse user = userService.getUserByName(nome);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        try {
            UserResponse user = userService.getUserByEmail(email);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/role")
    public ResponseEntity<List<UserResponse>> getUserByRole(@RequestParam NivelAcesso nivelAcesso) {
       try {
           List<UserResponse> users = userService.getUserByRole(nivelAcesso);
           return ResponseEntity.ok(users);
       }catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(null);
       }
    }
}
