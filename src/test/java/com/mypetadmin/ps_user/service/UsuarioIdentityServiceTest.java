package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.UsuarioNaoEncontradoException;
import com.mypetadmin.ps_user.exception.UsuarioRequisicaoInvalidaException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioIdentityServiceTest {

    @Mock UsuarioRepository repository;
    @Mock EmpresaClient empresaClient;
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repository, new UsuarioMapper(), empresaClient);
    }

    @Test
    void deveRetornarContextoMinimoParaLoginNormalizandoEmail() {
        Usuario usuario = usuario(StatusUsuario.ATIVO, RoleUsuario.ADMIN);
        when(repository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(usuario));

        var response = service.buscarIdentidadePorEmail(" ADMIN@EXAMPLE.COM ");

        assertThat(response.userId()).isEqualTo(usuario.getId());
        assertThat(response.empresaId()).isEqualTo(usuario.getEmpresaId());
        assertThat(response.email()).isEqualTo(usuario.getEmail());
        assertThat(response.status()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(response.roles()).containsExactly(RoleUsuario.ADMIN);
    }

    @Test
    void deveRetornarUsuarioInativoParaLoginDecidirBloqueio() {
        Usuario usuario = usuario(StatusUsuario.INATIVO, RoleUsuario.LOJA);
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var response = service.buscarIdentidadePorEmail(usuario.getEmail());

        assertThat(response.status()).isEqualTo(StatusUsuario.INATIVO);
    }

    @Test
    void deveRejeitarEmailVazioOuUsuarioInexistente() {
        assertThatThrownBy(() -> service.buscarIdentidadePorEmail("   "))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.buscarIdentidadePorEmail(null))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);

        when(repository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarIdentidadePorEmail("missing@example.com"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    private Usuario usuario(StatusUsuario status, RoleUsuario role) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(UUID.randomUUID());
        usuario.setNome("Usuário");
        usuario.setEmail(role.name().toLowerCase() + "@example.com");
        usuario.setStatus(status);
        usuario.setRoles(new HashSet<>(Set.of(role)));
        return usuario;
    }
}
