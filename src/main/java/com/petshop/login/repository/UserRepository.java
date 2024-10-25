package com.petshop.login.repository;

import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByNome(String nome);
    User findByEmail(String email);
    List<User> findByNivelAcesso(NivelAcesso nivelAcesso);
}
