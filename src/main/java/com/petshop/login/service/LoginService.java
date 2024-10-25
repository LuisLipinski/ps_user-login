package com.petshop.login.service;

import com.petshop.login.model.LoginRequest;
import com.petshop.login.model.LoginResponse;
import com.petshop.login.model.User;
import com.petshop.login.repository.UserRepository;
import com.petshop.login.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

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
}
