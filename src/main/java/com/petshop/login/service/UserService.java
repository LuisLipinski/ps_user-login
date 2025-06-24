package com.petshop.login.service;

import com.petshop.login.model.RegisterRequest;
import com.petshop.login.model.UpdateUserRequest;
import com.petshop.login.model.UserResponse;
import com.petshop.login.exception.ValidationException;
import com.petshop.login.model.DirectionField;
import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.SortField;
import com.petshop.login.model.User;
import com.petshop.login.repository.UserRepository;
import com.petshop.login.util.UserRoleHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRoleHierarchy userRoleHierarchy;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, UserRoleHierarchy userRoleHierarchy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRoleHierarchy = userRoleHierarchy;

    }

    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByNome(registerRequest.getNome()).isPresent()) {
            throw new ValidationException("Nome de usuário já existe.");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).stream().findFirst().isPresent()) {
            throw new ValidationException("Email já cadastrado.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByNome(currentUsername).orElseThrow(() -> new UsernameNotFoundException("Usuário logado não encontrado."));
        NivelAcesso requestedRole = registerRequest.getNivelAcesso();

        if (!userRoleHierarchy.canAssignRole(currentUser.getNivelAcesso(), requestedRole)) {
            throw new AccessDeniedException("Você não tem permissão para criar um usuário com o nível de acesso: " + requestedRole);
        }

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
        Sort sort = Sort.by(Sort.Direction.fromString(directionField.getDirection()), sortField.getField());
        return userRepository.findAll(sort).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getCurrentUser(String nome) {
        User user = userRepository.findByNome(nome).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(),user.getCriadoEm());
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToUserResponse);
    }

    public Optional<UserResponse> getUserByName(String nome) {
        return userRepository.findByNome(nome).map(this::convertToUserResponse);
    }

    public List<UserResponse> getUserByEmail(String email, SortField sortField, DirectionField direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction.getDirection()), sortField.getField());
        return userRepository.findByEmail(email, sort).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUserByRole(NivelAcesso nivelAcesso, SortField sortField, DirectionField direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction.getDirection()), sortField.getField());
        return userRepository.findByNivelAcesso(nivelAcesso, sort).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByNome(currentUsername).orElseThrow(() -> new UsernameNotFoundException("Usuário logado não encontrado."));

        NivelAcesso requestedRole;
        try {
            requestedRole = updateUserRequest.getNivelAcesso();
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Nível de acesso inválido.");
        }

        if (!userRoleHierarchy.canUpdateRole(currentUser.getNivelAcesso(), existingUser.getNivelAcesso(), requestedRole)) {
            throw new AccessDeniedException("Você não tem permissão para alterar o nível de acesso deste usuário para: " + requestedRole);
        }

        existingUser.setNome(updateUserRequest.getNome());
        existingUser.setEmail(updateUserRequest.getEmail());
        existingUser.setNivelAcesso(requestedRole);

        User updatedUser = userRepository.save(existingUser);
        return convertToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == 1) { // Supondo que o ID 1 seja um superadmin que não pode ser deletado
            throw new ValidationException("Este usuário não pode ser excluído.");
        }
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByNome(currentUsername).orElseThrow(() -> new UsernameNotFoundException("Usuário logado não encontrado."));

        if (!userRoleHierarchy.canDeleteUser(currentUser.getNivelAcesso(), userToDelete.getNivelAcesso())) {
            throw new AccessDeniedException("Você não tem permissão para excluir este usuário.");
        }

        userRepository.delete(userToDelete);
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByNome(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        if (!passwordEncoder.matches(oldPassword, user.getSenha())) {
            throw new ValidationException("Senha antiga incorreta.");
        }
        user.setSenha(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email, String nome) {
        List<User> users = userRepository.findByEmailAndNome(email, nome);
        if (users.isEmpty()) {
            throw new ValidationException("Nenhum usuário encontrado com o email e nome fornecidos.");
        }
        if (users.size() > 1) {
            throw new ValidationException("Múltiplos usuários encontrados com o mesmo email e nome.");
        }
        User user = users.get(0);
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetLink = "http://localhost:3000/reset-password?token=" + token; // Use a porta correta
        emailService.sendSimpleMessage(email, "Recuperação de Senha", "Clique no link para redefinir sua senha: " + resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ValidationException("Token de redefinição de senha inválido."));
        user.setSenha(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm());
    }

    @Override
    public UserDetails loadUserByUsername(String nome) throws UsernameNotFoundException {
        User user = userRepository.findByNome(nome)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o nome: " + nome));

        return new org.springframework.security.core.userdetails.User(
                user.getNome(),
                user.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getNivelAcesso().name()))
        );
    }
}