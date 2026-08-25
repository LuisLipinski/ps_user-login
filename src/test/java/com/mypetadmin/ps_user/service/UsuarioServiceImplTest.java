package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.dto.EmpresaStatusResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.EmailExistenteException;
import com.mypetadmin.ps_user.exception.OnboardingConflictException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private EmpresaClient empresaClient;

    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repository, new UsuarioMapper(), empresaClient);
    }

    @Test
    void deveCriarMasterDeFormaIdempotente() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        var request = new UsuarioMasterCreateRequestDTO(empresaId, onboardingId, "Luis", "LUIS@EXAMPLE.COM");

        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "AGUARDANDO_CONTRATO"));
        when(repository.existsByEmailIgnoreCase("luis@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(UUID.randomUUID());
            return usuario;
        });

        var response = service.criarMaster(request);

        assertThat(response.email()).isEqualTo("luis@example.com");
        assertThat(response.status()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(response.roles()).containsExactly(RoleUsuario.MASTER);
        verify(repository).saveAndFlush(any(Usuario.class));
    }

    @Test
    void deveRetornarMesmoUsuarioNoReplayDoOnboarding() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        Usuario existente = usuarioExistente(empresaId, onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.of(existente));

        var response = service.criarMaster(new UsuarioMasterCreateRequestDTO(
                empresaId, onboardingId, "Master", "MASTER@example.com"));

        assertThat(response.id()).isEqualTo(existente.getId());
        verify(empresaClient, never()).buscarStatusEmpresa(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarOnboardingReutilizadoComOutroUsuario() {
        UUID onboardingId = UUID.randomUUID();
        Usuario existente = usuarioExistente(UUID.randomUUID(), onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criarMaster(new UsuarioMasterCreateRequestDTO(
                UUID.randomUUID(), onboardingId, "Outro", "outro@example.com")))
                .isInstanceOf(OnboardingConflictException.class);
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "ATIVO"));
        when(repository.existsByEmailIgnoreCase("master@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criarMaster(new UsuarioMasterCreateRequestDTO(
                empresaId, onboardingId, "Master", "master@example.com")))
                .isInstanceOf(EmailExistenteException.class);
    }

    private Usuario usuarioExistente(UUID empresaId, UUID onboardingId, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(empresaId);
        usuario.setOnboardingId(onboardingId);
        usuario.setNome("Master");
        usuario.setEmail(email);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(RoleUsuario.MASTER);
        return usuario;
    }
}
