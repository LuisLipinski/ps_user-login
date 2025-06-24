package com.petshop.login.repository;

import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNome(String nome);
    List<User> findByEmail(String email, Sort sort);
    List<User> findByNivelAcesso(NivelAcesso nivelAcesso, Sort sort);
    List<User> findByEmail(String email);
    List<User> findByEmailAndNome(String email, String nome);
    Optional<User> findByResetToken(String resetToken);
}