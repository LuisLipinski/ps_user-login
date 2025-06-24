package com.petshop.login.service;

import com.petshop.login.exception.InvalidCredentialsException;
import com.petshop.login.model.LoginRequest;
import com.petshop.login.model.LoginResponse;
import com.petshop.login.model.User;
import com.petshop.login.repository.UserRepository;
import com.petshop.login.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getNome(), loginRequest.getSenha())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByNome(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            String token = jwtUtil.generateToken(userDetails);

            return new LoginResponse(user.getEmail(), user.getNome(), user.getCriadoEm(), token);

        } catch (AuthenticationException e) {
            logger.error("Erro na autenticação para o usuário {}: {}", loginRequest.getNome(), e.getMessage());
            throw new InvalidCredentialsException("Usuario ou senha invalido");
        }
    }
}