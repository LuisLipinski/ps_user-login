package com.petshop.login.service;

import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.RegisterRequest;
import com.petshop.login.model.User;
import com.petshop.login.model.UserResponse;
import com.petshop.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {

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
}
