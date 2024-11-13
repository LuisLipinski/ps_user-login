package com.petshop.login.service;

import com.petshop.login.exception.ValidationException;
import com.petshop.login.model.*;
import com.petshop.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('MASTER' or hasRole('ADMIN')")
    public UserResponse register(RegisterRequest registerRequest) {
        //valida as regras de registro
        UserValidation.validateRegisterRequest(registerRequest);
        //obtem o usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().toString();

        //verifica a Role do usuario atual
        if (currentUserRole.contains("ROLE_MASTER")) {
            //master cria todos os usuarios
            return createUser(registerRequest);
        } else if (currentUserRole.contains("ROLE_ADMIN")) {
            //admin não pode criar master e admin
            if (registerRequest.getNivelAcesso() == NivelAcesso.MASTER ||
                    registerRequest.getNivelAcesso() == NivelAcesso.ADMIN) {
                throw new ValidationException("Você não tem permissão de criar um usuário com perfil Master e Admin.");
            }
            return createUser(registerRequest);
        } else {
            throw new ValidationException("Você não tem permissão para cadastrar usuários.");
        }
    }


    private UserResponse createUser(RegisterRequest registerRequest) {
        //cadastra o usuário
        User newUser = new User();
        newUser.setNome(registerRequest.getNome());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setSenha(passwordEncoder.encode(registerRequest.getSenha()));
        newUser.setNivelAcesso(registerRequest.getNivelAcesso());
        newUser.setCriadoEm(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        return new UserResponse(newUser.getId(), newUser.getNome(), newUser.getEmail(), newUser.getNivelAcesso(), newUser.getCriadoEm());
    }


    public List<UserResponse> getAllUsersSorted(SortField sortField, DirectionField directionField) {
        Sort sort = Sort.by(Sort.Direction.fromString(directionField.getDirection()),sortField.getField());
        return userRepository.findAll(sort).stream()
                .map(user -> new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()))
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user != null ? new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()) : null;
    }

    public UserResponse getUserByName(String nome) {
        User user = userRepository.findByNome(nome);
        return user != null ? new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()) : null;
    }

    public List<UserResponse> getUserByEmail(String email, SortField sortField, DirectionField direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction.getDirection()), sortField.getField());
       return userRepository.findByEmail(email, sort).stream()
               .map(user -> new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()))
               .collect(Collectors.toList());
    }

    public List<UserResponse> getUserByRole(NivelAcesso nivelAcesso, SortField sortField, DirectionField direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction.getDirection()), sortField.getField());
        return userRepository.findByNivelAcesso(nivelAcesso, sort).stream()
                .map(user -> new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()))
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().toString();

        User existingUser = userRepository.findById(id).orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        if(currentUserRole.contains("ROLE_MASTER")) {
            return updateUserInRepository(existingUser, updateUserRequest);
        } else if(currentUserRole.contains("ROLE_ADMIN")) {
            if ("MASTER".equals(existingUser.getNivelAcesso().name()) ||
                "ADMIN".equals(existingUser.getNivelAcesso().name())) {
                throw new ValidationException("Voce não tem permissão para editar usuários.");
            }
            return updateUserInRepository(existingUser, updateUserRequest);
        } else {
            throw new ValidationException("Você não tem permissão para editar usuário");
        }
    }

    private UserResponse updateUserInRepository(User user, UpdateUserRequest updateUserRequest) {

        try{
            NivelAcesso nivelAcesso = NivelAcesso.valueOf(updateUserRequest.getNivelAcesso().toUpperCase());
            user.setNivelAcesso(nivelAcesso);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Nível de acesso inválido. Níveis permitidos: MASTER, BANHO, ESTOQUE, CRECHE, VETERINARIO, ADMIN, LOJA.");
        }
        user.setNome(updateUserRequest.getNome());
        user.setEmail(updateUserRequest.getEmail());

        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm());
    }

    public void deleteUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().toString();

        if (id == 2) {
            throw new ValidationException("Esse usuário nunca pode ser excluido");
        }

        User userToDeleter = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (currentUserRole.contains("ROLE_MASTER")) {
            userRepository.delete(userToDeleter);
        } else if (currentUserRole.contains("ROLE_ADMIN")) {
            if("MASTER".equals(userToDeleter.getNivelAcesso().name()) ||
                "ADMIN".equals(userToDeleter.getNivelAcesso().name())) {
                throw new ValidationException("Você não tem permissão para excluir esse usuário");
            }
            userRepository.delete(userToDeleter);
        }else {
            throw new ValidationException("Você não tem permissão para excluir usuário");
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByNome(username);
        if(user != null) {
            if(!passwordEncoder.matches(oldPassword, user.getSenha())) {
                throw new ValidationException("Senha antiga incorreta.");
            }

            user.setSenha(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new ValidationException("Usuário não encontrado.");
        }
    }

    public String generateResetToken(String email, String nome) {
        List<User> users = userRepository.findByEmailAndNome(email, nome);
        if (users.isEmpty()) {
            throw new ValidationException("Nenhum usuário encontrado com o email: " + email + " e nome: " + nome);
        } else if (users.size() > 1) {
            throw new ValidationException("Mais de um usuário encontrado com o email: " + email + " e nome: " + nome);
        }

        User user = users.get(0);
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(token);
        if (userOptional.isEmpty()) {
            throw new ValidationException("Token inválido.");
        }

        User user = userOptional.get();
        user.setSenha(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }
}
