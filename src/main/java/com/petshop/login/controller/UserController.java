package com.petshop.login.controller;

import com.petshop.login.model.*;
import com.petshop.login.exception.*;
import com.petshop.login.model.DirectionField;
import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.SortField;
import com.petshop.login.service.EmailService;
import com.petshop.login.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuario")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação de entrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserResponse newUser = userService.register(registerRequest);
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody @Valid UpdateUserRequest updateUserRequest) {
        UserResponse updatedUser = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(updatedUser);

    }

    @PutMapping("/edit/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email, @RequestParam String nome) {
        userService.forgotPassword(email, nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        userService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                          @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
        List<UserResponse> users = userService.getAllUsersSorted(sortField, direction);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String nome = ((UserDetails)authentication.getPrincipal()).getUsername();
        UserResponse resp = userService.getCurrentUser(nome);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return userService.getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Usuário não encontrado.")));
    }

    @GetMapping("/name")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<?> getByName(@RequestParam String nome) {
        return userService.getUserByName(nome)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Usuário não encontrado.")));
    }

    @GetMapping("/email")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUserByEmail(@RequestParam String email,
                                                             @RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                             @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
        List<UserResponse> users = userService.getUserByEmail(email, sortField, direction);
                return ResponseEntity.ok(users);

    }

    @GetMapping("/role")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUserByRole(@RequestParam NivelAcesso nivelAcesso,
                                                            @RequestParam(required = false, defaultValue = "NOME") SortField sortField,
                                                            @RequestParam(required = false, defaultValue = "ASC") DirectionField direction) {
        List<UserResponse> users = userService.getUserByRole(nivelAcesso, sortField, direction);
                return ResponseEntity.ok(users);
    }
}