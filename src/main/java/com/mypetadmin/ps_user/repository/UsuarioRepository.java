package com.mypetadmin.ps_user.repository;

import com.mypetadmin.ps_user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByOnboardingId(UUID onboardingId);

    boolean existsByEmailIgnoreCase(String email);
}
