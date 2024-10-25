package com.petshop.login.service;

import com.petshop.login.model.*;
import com.petshop.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
