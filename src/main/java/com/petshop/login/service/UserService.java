package com.petshop.login.service;

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
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('MASTER' or hasRole('ADMIN')")
    public UserResponse register(RegisterRequest registerRequest) {
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
                throw new RuntimeException("Você não tem permissão de criar um usuário com perfil Master e Admin.");
            }
            return createUser(registerRequest);
        } else {
            throw new RuntimeException("Você não tem permissão para cadastrar usuários.");
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

    public UserResponse updateUser(Long id, RegisterRequest registerRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().toString();

        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(currentUserRole.contains("ROLE_MASTER")) {
            return updateUserInRepository(existingUser, registerRequest);
        } else if(currentUserRole.contains("ROLE_ADMIN")) {
            if ("MASTER".equals(existingUser.getNivelAcesso().name()) ||
                "ADMIN".equals(existingUser.getNivelAcesso().name())) {
                throw new RuntimeException("Voce não tem permissão para editar usuários.");
            }
            return updateUserInRepository(existingUser, registerRequest);
        } else {
            throw new RuntimeException("Você não tem permissão para editar usuário");
        }
    }

    private UserResponse updateUserInRepository(User user, RegisterRequest registerRequest) {
        user.setNome(registerRequest.getNome());
        user.setEmail(registerRequest.getEmail());
        user.setNivelAcesso(registerRequest.getNivelAcesso());

        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm());
    }

    public void deleteUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = authentication.getAuthorities().toString();

        if (id == 2) {
            throw new RuntimeException("Esse usuário nunca pode ser excluido");
        }

        User userToDeleter = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (currentUserRole.contains("ROLE_MASTER")) {
            userRepository.delete(userToDeleter);
        } else if (currentUserRole.contains("ROLE_ADMIN")) {
            if("MASTER".equals(userToDeleter.getNivelAcesso().name()) ||
                "ADMIN".equals(userToDeleter.getNivelAcesso().name())) {
                throw new RuntimeException("Você não tem permissão para excluir esse usuário");
            }
            userRepository.delete(userToDeleter);
        }else {
            throw new RuntimeException("Você não tem permissão para excluir usuário");
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByNome(username);
        if(user != null) {
            if(!passwordEncoder.matches(oldPassword, user.getSenha())) {
                throw new RuntimeException("Senha antiga incorreta.");
            }

            user.setSenha(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuário não encontrado.");
        }
    }
}
