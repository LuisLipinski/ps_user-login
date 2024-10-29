package com.petshop.login.controller;

import com.petshop.login.model.*;
import com.petshop.login.service.EmailService;
import com.petshop.login.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse newUser = userService.register(registerRequest);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @PutMapping("/edit/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse updatedUser = userService.updateUser(id, registerRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/edit/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.changePassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email, @RequestParam String nome) {
        logger.debug("Received forgot password request for email: {} and nome: {}", email, nome);

        try {
            // Generate reset token and save to user record
            String resetToken = userService.generateResetToken(email, nome);
            logger.debug("Generated reset token: {}", resetToken);

            // Send email with reset link
            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
            emailService.sendSimpleMessage(email, "Password Reset Request", resetLink);
            logger.debug("Sent reset link to email: {}", email);

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error processing forgot password request for email: {} and nome: {}", email, nome, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }


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
    public ResponseEntity<List<UserResponse>> getUserByEmail(@RequestParam String email,
                                                             @RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                             @RequestParam(required = false, defaultValue = "NOME") DirectionField direction) {
        try {
            List<UserResponse> users = userService.getUserByEmail(email, sortField, direction);
            return users != null ? ResponseEntity.ok(users) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/role")
    public ResponseEntity<List<UserResponse>> getUserByRole(@RequestParam NivelAcesso nivelAcesso,
                                                            @RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                            @RequestParam(required = false, defaultValue = "NOME") DirectionField direction) {
       try {
           List<UserResponse> users = userService.getUserByRole(nivelAcesso, sortField, direction);
           return ResponseEntity.ok(users);
       }catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(null);
       }
    }
}
