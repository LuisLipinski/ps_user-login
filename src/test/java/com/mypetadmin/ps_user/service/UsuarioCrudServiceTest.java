package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.dto.UsuarioRoleUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioStatusUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioUpdateRequestDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.EmailExistenteException;
import com.mypetadmin.ps_user.exception.PrimeiroMasterProtegidoException;
import com.mypetadmin.ps_user.exception.UsuarioNaoEncontradoException;
import com.mypetadmin.ps_user.exception.UsuarioOperacaoNaoPermitidaException;
import com.mypetadmin.ps_user.exception.UsuarioRequisicaoInvalidaException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioCrudServiceTest {

    @Mock UsuarioRepository repository;
    @Mock EmpresaClient empresaClient;
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repository, new UsuarioMapper(), empresaClient);
    }

    @Test
    void deveBuscarUsuarioSomenteNoMesmoTenant() {
        Usuario actor = usuario(RoleUsuario.MASTER, false);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        var response = service.buscarUsuario(actor.getId(), alvo.getId());

        assertThat(response.id()).isEqualTo(alvo.getId());
        assertThat(response.empresaId()).isEqualTo(actor.getEmpresaId());
    }

    @Test
    void deveOcultarUsuarioDeOutroTenantComoNaoEncontrado() {
        Usuario actor = usuario(RoleUsuario.MASTER, false);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(any(), eq(actor.getEmpresaId()))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarUsuario(actor.getId(), UUID.randomUUID()))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveListarComFiltrosPaginacaoEOrdenacao() {
        Usuario actor = usuario(RoleUsuario.ADMIN, false);
        Usuario alvo = usuario(RoleUsuario.BANHO, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.buscarPorFiltros(eq(actor.getEmpresaId()), eq("Maria"), eq("pet@"),
                eq(StatusUsuario.ATIVO), eq(RoleUsuario.BANHO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(alvo)));

        var response = service.listarUsuarios(actor.getId(), " Maria ", " pet@ ", StatusUsuario.ATIVO,
                RoleUsuario.BANHO, 0, 10, "email", "desc");

        assertThat(response.content()).hasSize(1);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).buscarPorFiltros(eq(actor.getEmpresaId()), eq("Maria"), eq("pet@"),
                eq(StatusUsuario.ATIVO), eq(RoleUsuario.BANHO), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("email").getDirection().name()).isEqualTo("DESC");
    }

    @Test
    void deveAplicarDefaultsNaListagem() {
        Usuario actor = usuario(RoleUsuario.LOJA, false);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.buscarPorFiltros(eq(actor.getEmpresaId()), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        service.listarUsuarios(actor.getId(), " ", null, null, null, 0, 20, null, null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).buscarPorFiltros(eq(actor.getEmpresaId()), eq(null), eq(null), eq(null), eq(null), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("nome").isAscending()).isTrue();
    }

    @Test
    void deveValidarParametrosDePaginacaoEOrdenacao() {
        Usuario actor = usuario(RoleUsuario.MASTER, false);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.listarUsuarios(actor.getId(), null, null, null, null, -1, 20, "nome", "asc"))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.listarUsuarios(actor.getId(), null, null, null, null, 0, 0, "nome", "asc"))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.listarUsuarios(actor.getId(), null, null, null, null, 0, 101, "nome", "asc"))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.listarUsuarios(actor.getId(), null, null, null, null, 0, 20, "senha", "asc"))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.listarUsuarios(actor.getId(), null, null, null, null, 0, 20, "nome", "lado"))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
    }

    @Test
    void masterDeveAtualizarNomeEEmail() {
        Usuario actor = usuario(RoleUsuario.MASTER, true);
        Usuario alvo = usuario(RoleUsuario.ADMIN, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));
        when(repository.existsByEmailIgnoreCaseAndIdNot("novo@example.com", alvo.getId())).thenReturn(false);
        when(repository.saveAndFlush(alvo)).thenReturn(alvo);

        var response = service.atualizarUsuario(actor.getId(), alvo.getId(),
                new UsuarioUpdateRequestDTO(" Novo Nome ", " NOVO@EXAMPLE.COM "));

        assertThat(response.nome()).isEqualTo("Novo Nome");
        assertThat(response.email()).isEqualTo("novo@example.com");
    }

    @Test
    void deveRejeitarAtualizacaoVaziaNomeVazioEEmailVazio() {
        Usuario actor = usuario(RoleUsuario.MASTER, true);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(), new UsuarioUpdateRequestDTO(null, null)))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(), new UsuarioUpdateRequestDTO("   ", null)))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(), new UsuarioUpdateRequestDTO(null, "   ")))
                .isInstanceOf(UsuarioRequisicaoInvalidaException.class);
    }

    @Test
    void deveRejeitarEmailDuplicadoInclusiveEmCorridaDeBanco() {
        Usuario actor = usuario(RoleUsuario.MASTER, true);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));
        when(repository.existsByEmailIgnoreCaseAndIdNot("duplicado@example.com", alvo.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(),
                new UsuarioUpdateRequestDTO(null, "duplicado@example.com")))
                .isInstanceOf(EmailExistenteException.class);

        when(repository.existsByEmailIgnoreCaseAndIdNot("outro@example.com", alvo.getId())).thenReturn(false);
        when(repository.saveAndFlush(alvo)).thenThrow(new DataIntegrityViolationException("race"));
        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(),
                new UsuarioUpdateRequestDTO(null, "outro@example.com")))
                .isInstanceOf(EmailExistenteException.class);
    }

    @Test
    void adminPodeGerenciarApenasPerfisOperacionais() {
        Usuario actor = usuario(RoleUsuario.ADMIN, false);
        Usuario operacional = usuario(RoleUsuario.CRECHE, false);
        operacional.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(operacional.getId(), actor.getEmpresaId())).thenReturn(Optional.of(operacional));
        when(repository.saveAndFlush(operacional)).thenReturn(operacional);

        var response = service.atualizarStatus(actor.getId(), operacional.getId(),
                new UsuarioStatusUpdateRequestDTO(StatusUsuario.INATIVO));
        assertThat(response.status()).isEqualTo(StatusUsuario.INATIVO);

        Usuario adminAlvo = usuario(RoleUsuario.ADMIN, false);
        adminAlvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findByIdAndEmpresaId(adminAlvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(adminAlvo));
        assertThatThrownBy(() -> service.atualizarStatus(actor.getId(), adminAlvo.getId(),
                new UsuarioStatusUpdateRequestDTO(StatusUsuario.INATIVO)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void primeiroMasterNaoPodeSerInativadoNemPerderRoleMaster() {
        Usuario actor = usuario(RoleUsuario.MASTER, true);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(actor.getId(), actor.getEmpresaId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.atualizarStatus(actor.getId(), actor.getId(),
                new UsuarioStatusUpdateRequestDTO(StatusUsuario.INATIVO)))
                .isInstanceOf(PrimeiroMasterProtegidoException.class);
        assertThatThrownBy(() -> service.atualizarRole(actor.getId(), actor.getId(),
                new UsuarioRoleUpdateRequestDTO(RoleUsuario.ADMIN)))
                .isInstanceOf(PrimeiroMasterProtegidoException.class);
    }

    @Test
    void masterPodeAlterarRoleDeUsuarioNaoPrimario() {
        Usuario actor = usuario(RoleUsuario.MASTER, true);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));
        when(repository.saveAndFlush(alvo)).thenReturn(alvo);

        var response = service.atualizarRole(actor.getId(), alvo.getId(), new UsuarioRoleUpdateRequestDTO(RoleUsuario.ADMIN));

        assertThat(response.roles()).containsExactly(RoleUsuario.ADMIN);
    }

    @Test
    void adminPodeTrocarSomenteEntreRolesOperacionais() {
        Usuario actor = usuario(RoleUsuario.ADMIN, false);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));
        when(repository.saveAndFlush(alvo)).thenReturn(alvo);

        var response = service.atualizarRole(actor.getId(), alvo.getId(), new UsuarioRoleUpdateRequestDTO(RoleUsuario.HOTEL));
        assertThat(response.roles()).containsExactly(RoleUsuario.HOTEL);

        alvo.setRoles(new HashSet<>(Set.of(RoleUsuario.LOJA)));
        assertThatThrownBy(() -> service.atualizarRole(actor.getId(), alvo.getId(), new UsuarioRoleUpdateRequestDTO(RoleUsuario.ADMIN)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void perfilOperacionalNaoPodeGerenciarUsuario() {
        Usuario actor = usuario(RoleUsuario.BANHO, false);
        Usuario alvo = usuario(RoleUsuario.LOJA, false);
        alvo.setEmpresaId(actor.getEmpresaId());
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.atualizarUsuario(actor.getId(), alvo.getId(), new UsuarioUpdateRequestDTO("Nome", null)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
        verify(repository, never()).saveAndFlush(alvo);
    }

    private Usuario usuario(RoleUsuario role, boolean primaryMaster) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(UUID.randomUUID());
        usuario.setNome("Usuário");
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setPrimaryMaster(primaryMaster);
        usuario.setRoles(new HashSet<>(Set.of(role)));
        return usuario;
    }
}
