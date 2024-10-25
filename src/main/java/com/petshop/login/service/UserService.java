package com.petshop.login.service;

import com.petshop.login.model.*;
import com.petshop.login.repository.UserRepository;
import com.petshop.login.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByNome(loginRequest.getNome()); //busca pelo nome
        if(user != null && passwordEncoder.matches(loginRequest.getSenha(), user.getSenha())) { //valida se o usuário não é nulo e se o password esta correto com o banco de dados
            String token = jwtUtil.generateToken(user.getNome(), user.getNivelAcesso().name()); //gera o token para o usuario
            return new LoginResponse(user.getEmail(), user.getNome(), user.getCriadoEm(), token);
        }
        throw new RuntimeException("Dados invalidos");
    }



    @PreAuthorize("hasRole('MASTER' or hasRole('ADMIN')")
    public User register(RegisterRequest registerRequest) {
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


    private User createUser(RegisterRequest registerRequest) {
        //cadastra o usuário
        User newUser = new User();
        newUser.setNome(registerRequest.getNome());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setSenha(passwordEncoder.encode(registerRequest.getSenha()));
        newUser.setNivelAcesso(registerRequest.getNivelAcesso());
        return userRepository.save(newUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
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

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()) : null;
    }

    public List<UserResponse> getUserByRole(NivelAcesso nivelAcesso) {
        return userRepository.findByNivelAcesso(nivelAcesso).stream()
                .map(user -> new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getNivelAcesso(), user.getCriadoEm()))
                .collect(Collectors.toList());
    }
}
