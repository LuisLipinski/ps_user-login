package com.petshop.login.controller;

import com.petshop.login.exception.ErrorResponse;
import com.petshop.login.exception.ValidationException;
import com.petshop.login.model.*;
import com.petshop.login.service.EmailService;
import com.petshop.login.service.UserService;
import com.petshop.login.service.UserValidation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação de entrada",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest registerRequest) {
        UserValidation.validateRegisterRequest(registerRequest);
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
                                                   @RequestBody UpdateUserRequest updateUserRequest) {
        if (updateUserRequest.getNome() == null || updateUserRequest.getNome().isEmpty()) {
            throw new ValidationException("Nome não pode ser vazio");
        } else {
            UserValidation.validateUsername(updateUserRequest.getNome());
            if (updateUserRequest.getEmail() == null || updateUserRequest.getEmail().isEmpty()) {
                throw new ValidationException("Email não pode ser vazio");
            } else {
                UserValidation.validateEmail(updateUserRequest.getEmail());
                UserValidation.validateNivelAcesso(updateUserRequest.getNivelAcesso());
                try {
                    UserResponse updatedUser = userService.updateUser(id, updateUserRequest);
                    return ResponseEntity.ok(updatedUser);
                } catch (RuntimeException e) {
                    throw new ValidationException("Dados invalidos");
                }
            }
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/edit/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        UserValidation.validatePassword(changePasswordRequest.getNewPassword());
        try {
            userService.changePassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ValidationException("Senha antiga invalida");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email, @RequestParam String nome) {
        logger.debug("Received forgot password request for email: {} and nome: {}", email, nome);

        UserValidation.validateEmail(email);

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
        UserValidation.validatePassword(newPassword);
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ValidationException("Token invalido");
        }
    }

    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ValidationException("Usuario não encontrado");
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
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);

            return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Usuário não encontrado."));
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/name")
    public ResponseEntity<?> getByName(@RequestParam String nome) {
        try {
            UserResponse user = userService.getUserByName(nome);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Usuário não encontrado."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    @GetMapping("/email")
    public ResponseEntity<List<UserResponse>> getUserByEmail(@RequestParam String email,
                                                             @RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                             @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
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
                                                            @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
       try {
           List<UserResponse> users = userService.getUserByRole(nivelAcesso, sortField, direction);
           return ResponseEntity.ok(users);
       }catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(null);
       }
    }
}
