package com.mypetadmin.ps_user.repository;

import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByOnboardingId(UUID onboardingId);

    Optional<Usuario> findByIdAndEmpresaId(UUID id, UUID empresaId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByEmpresaIdAndPrimaryMasterTrue(UUID empresaId);

    @Query(value = """
            select distinct u
            from Usuario u
            left join u.roles r
            where u.empresaId = :empresaId
              and (:nome is null or lower(u.nome) like lower(concat('%', :nome, '%')))
              and (:email is null or lower(u.email) like lower(concat('%', :email, '%')))
              and (:status is null or u.status = :status)
              and (:role is null or r = :role)
            """,
            countQuery = """
            select count(distinct u)
            from Usuario u
            left join u.roles r
            where u.empresaId = :empresaId
              and (:nome is null or lower(u.nome) like lower(concat('%', :nome, '%')))
              and (:email is null or lower(u.email) like lower(concat('%', :email, '%')))
              and (:status is null or u.status = :status)
              and (:role is null or r = :role)
            """)
    Page<Usuario> buscarPorFiltros(
            @Param("empresaId") UUID empresaId,
            @Param("nome") String nome,
            @Param("email") String email,
            @Param("status") StatusUsuario status,
            @Param("role") RoleUsuario role,
            Pageable pageable);
}
