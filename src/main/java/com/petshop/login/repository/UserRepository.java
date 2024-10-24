package com.petshop.login.repository;

import com.petshop.login.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByNome(String nome);
}
