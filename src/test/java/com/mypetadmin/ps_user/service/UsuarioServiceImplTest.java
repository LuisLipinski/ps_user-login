package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.dto.EmpresaStatusResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.EmailExistenteException;
import com.mypetadmin.ps_user.exception.EmpresaIndisponivelException;
import com.mypetadmin.ps_user.exception.EmpresaNaoEncontradaException;
import com.mypetadmin.ps_user.exception.OnboardingConflictException;
import com.mypetadmin.ps_user.exception.PrimaryMasterExistenteException;
import com.mypetadmin.ps_user.exception.PrimeiroMasterProtegidoException;
import com.mypetadmin.ps_user.exception.UsuarioNaoEncontradoException;
import com.mypetadmin.ps_user.exception.UsuarioOperacaoNaoPermitidaException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock UsuarioRepository repository;
    @Mock EmpresaClient empresaClient;
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repository, new UsuarioMapper(), empresaClient);
    }

    @Test
    void deveCriarPrimaryMasterNormalizandoEmail() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        var request = new UsuarioMasterCreateRequestDTO(empresaId, onboardingId, "  Luis  ", " LUIS@EXAMPLE.COM ");

        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "AGUARDANDO_CONTRATO"));
        when(repository.existsByEmailIgnoreCase("luis@example.com")).thenReturn(false);
        mockSave();

        var response = service.criarMaster(request);

        assertThat(response.email()).isEqualTo("luis@example.com");
        assertThat(response.nome()).isEqualTo("Luis");
        assertThat(response.status()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(response.primaryMaster()).isTrue();
        assertThat(response.roles()).containsExactly(RoleUsuario.MASTER);
    }

    @Test
    void deveImpedirSegundoPrimaryMasterDaEmpresa() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "ATIVO"));
        when(repository.existsByEmpresaIdAndPrimaryMasterTrue(empresaId)).thenReturn(true);

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(PrimaryMasterExistenteException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveDetectarCorridaDePrimaryMaster() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty(), Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "ATIVO"));
        when(repository.existsByEmpresaIdAndPrimaryMasterTrue(empresaId)).thenReturn(false, true);
        when(repository.existsByEmailIgnoreCase("master@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("primary master race"));

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(PrimaryMasterExistenteException.class);
    }

    @ParameterizedTest
    @EnumSource(RoleUsuario.class)
    void masterPodeCriarQualquerPerfil(RoleUsuario novaRole) {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        mockSave();

        var response = service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Novo Usuário", " NOVO@EXAMPLE.COM ", novaRole));

        assertThat(response.empresaId()).isEqualTo(actor.getEmpresaId());
        assertThat(response.email()).isEqualTo("novo@example.com");
        assertThat(response.primaryMaster()).isFalse();
        assertThat(response.roles()).containsExactly(novaRole);
    }

    @ParameterizedTest
    @EnumSource(value = RoleUsuario.class, names = {"LOJA", "VETERINARIO", "BANHO", "HOTEL", "CRECHE"})
    void adminPodeCriarSomentePerfisOperacionais(RoleUsuario novaRole) {
        Usuario actor = usuario(RoleUsuario.ADMIN, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        mockSave();

        var response = service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Operacional", "operacional@example.com", novaRole));

        assertThat(response.roles()).containsExactly(novaRole);
    }

    @ParameterizedTest
    @EnumSource(value = RoleUsuario.class, names = {"MASTER", "ADMIN"})
    void adminNaoPodeCriarMasterNemAdmin(RoleUsuario novaRole) {
        Usuario actor = usuario(RoleUsuario.ADMIN, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Bloqueado", "bloqueado@example.com", novaRole)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = RoleUsuario.class, names = {"LOJA", "VETERINARIO", "BANHO", "HOTEL", "CRECHE"})
    void perfilOperacionalNaoPodeCriarUsuario(RoleUsuario actorRole) {
        Usuario actor = usuario(actorRole, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Bloqueado", "bloqueado@example.com", RoleUsuario.LOJA)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void usuarioInativoNaoPodeGerenciarUsuarios() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.INATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Novo", "novo@example.com", RoleUsuario.ADMIN)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void deveRejeitarActorInexistente() {
        UUID actorId = UUID.randomUUID();
        when(repository.findById(actorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarUsuario(actorId, new UsuarioCreateRequestDTO(
                "Novo", "novo@example.com", RoleUsuario.LOJA)))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarEmailDuplicadoNaCriacaoGerenciada() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.existsByEmailIgnoreCase("duplicado@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Duplicado", "duplicado@example.com", RoleUsuario.ADMIN)))
                .isInstanceOf(EmailExistenteException.class);
    }

    @Test
    void deveTraduzirConflitoDeBancoNaCriacaoGerenciadaComoEmailDuplicado() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Duplicado", "duplicado@example.com", RoleUsuario.LOJA)))
                .isInstanceOf(EmailExistenteException.class);
    }

    @ParameterizedTest
    @EnumSource(RoleUsuario.class)
    void masterPodeExcluirQualquerPerfilExcetoPrimaryMaster(RoleUsuario alvoRole) {
        Usuario actor = usuario(RoleUsuario.MASTER, true, StatusUsuario.ATIVO);
        Usuario alvo = usuario(actor.getEmpresaId(), alvoRole, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        service.excluirUsuario(actor.getId(), alvo.getId());

        verify(repository).delete(alvo);
    }

    @ParameterizedTest
    @EnumSource(value = RoleUsuario.class, names = {"LOJA", "VETERINARIO", "BANHO", "HOTEL", "CRECHE"})
    void adminPodeExcluirPerfisOperacionais(RoleUsuario alvoRole) {
        Usuario actor = usuario(RoleUsuario.ADMIN, false, StatusUsuario.ATIVO);
        Usuario alvo = usuario(actor.getEmpresaId(), alvoRole, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        service.excluirUsuario(actor.getId(), alvo.getId());

        verify(repository).delete(alvo);
    }

    @ParameterizedTest
    @EnumSource(value = RoleUsuario.class, names = {"MASTER", "ADMIN"})
    void adminNaoPodeExcluirMasterNemAdmin(RoleUsuario alvoRole) {
        Usuario actor = usuario(RoleUsuario.ADMIN, false, StatusUsuario.ATIVO);
        Usuario alvo = usuario(actor.getEmpresaId(), alvoRole, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.excluirUsuario(actor.getId(), alvo.getId()))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void perfilOperacionalNaoPodeExcluirUsuario() {
        Usuario actor = usuario(RoleUsuario.LOJA, false, StatusUsuario.ATIVO);
        Usuario alvo = usuario(actor.getEmpresaId(), RoleUsuario.CRECHE, false, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.excluirUsuario(actor.getId(), alvo.getId()))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void ninguemPodeExcluirPrimaryMaster() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        Usuario alvo = usuario(actor.getEmpresaId(), RoleUsuario.MASTER, true, StatusUsuario.ATIVO);
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvo.getId(), actor.getEmpresaId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.excluirUsuario(actor.getId(), alvo.getId()))
                .isInstanceOf(PrimeiroMasterProtegidoException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void naoPodeExcluirUsuarioDeOutraEmpresa() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        UUID alvoId = UUID.randomUUID();
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(repository.findByIdAndEmpresaId(alvoId, actor.getEmpresaId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluirUsuario(actor.getId(), alvoId))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarUsuarioComMaisDeUmaRoleNaGestao() {
        Usuario actor = usuario(RoleUsuario.MASTER, false, StatusUsuario.ATIVO);
        actor.setRoles(new HashSet<>(Set.of(RoleUsuario.MASTER, RoleUsuario.ADMIN)));
        when(repository.findById(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> service.criarUsuario(actor.getId(), new UsuarioCreateRequestDTO(
                "Novo", "novo@example.com", RoleUsuario.LOJA)))
                .isInstanceOf(UsuarioOperacaoNaoPermitidaException.class);
    }

    @Test
    void deveRetornarMesmoUsuarioNoReplayDoOnboarding() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        Usuario existente = usuarioExistente(empresaId, onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.of(existente));

        var response = service.criarMaster(new UsuarioMasterCreateRequestDTO(empresaId, onboardingId, "Master", "MASTER@example.com"));

        assertThat(response.id()).isEqualTo(existente.getId());
        verify(empresaClient, never()).buscarStatusEmpresa(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarReplayComEmpresaDiferente() {
        UUID onboardingId = UUID.randomUUID();
        Usuario existente = usuarioExistente(UUID.randomUUID(), onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criarMaster(new UsuarioMasterCreateRequestDTO(
                UUID.randomUUID(), onboardingId, "Outro", "master@example.com")))
                .isInstanceOf(OnboardingConflictException.class);
    }

    @Test
    void deveRejeitarReplayComEmailDiferente() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        Usuario existente = usuarioExistente(empresaId, onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criarMaster(new UsuarioMasterCreateRequestDTO(
                empresaId, onboardingId, "Master", "outro@example.com")))
                .isInstanceOf(OnboardingConflictException.class);
    }

    @Test
    void deveRejeitarEmailDuplicadoNoOnboarding() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "ATIVO"));
        when(repository.existsByEmailIgnoreCase("master@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmailExistenteException.class);
    }

    @Test
    void deveRejeitarEmpresaNaoEncontradaPorRespostaNula() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(null);

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmpresaNaoEncontradaException.class);
    }

    @Test
    void deveRejeitarEmpresaComIdDivergente() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(UUID.randomUUID(), "ATIVO"));

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmpresaNaoEncontradaException.class);
    }

    @Test
    void deveTraduzir404DaEmpresa() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmpresaNaoEncontradaException.class);
    }

    @Test
    void deveFalharFechadoQuandoEmpresaIndisponivel() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        FeignException falha = mock(FeignException.class);
        when(falha.status()).thenReturn(503);
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenThrow(falha);

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmpresaIndisponivelException.class);
    }

    @Test
    void deveResolverCorridaDeOnboardingComoReplayIdempotente() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        Usuario concorrente = usuarioExistente(empresaId, onboardingId, "master@example.com");
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty(), Optional.of(concorrente));
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "AGUARDANDO_CONTRATO"));
        when(repository.existsByEmailIgnoreCase("master@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("race"));

        var response = service.criarMaster(request(empresaId, onboardingId));

        assertThat(response.id()).isEqualTo(concorrente.getId());
    }

    @Test
    void deveTraduzirConflitoDeBancoSemOnboardingConcorrenteComoEmailDuplicado() {
        UUID empresaId = UUID.randomUUID();
        UUID onboardingId = UUID.randomUUID();
        when(repository.findByOnboardingId(onboardingId)).thenReturn(Optional.empty(), Optional.empty());
        when(empresaClient.buscarStatusEmpresa(empresaId)).thenReturn(new EmpresaStatusResponseDTO(empresaId, "ATIVO"));
        when(repository.existsByEmailIgnoreCase("master@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.criarMaster(request(empresaId, onboardingId)))
                .isInstanceOf(EmailExistenteException.class);
    }

    private void mockSave() {
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(UUID.randomUUID());
            return usuario;
        });
    }

    private UsuarioMasterCreateRequestDTO request(UUID empresaId, UUID onboardingId) {
        return new UsuarioMasterCreateRequestDTO(empresaId, onboardingId, "Master", "master@example.com");
    }

    private Usuario usuario(RoleUsuario role, boolean primaryMaster, StatusUsuario status) {
        return usuario(UUID.randomUUID(), role, primaryMaster, status);
    }

    private Usuario usuario(UUID empresaId, RoleUsuario role, boolean primaryMaster, StatusUsuario status) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(empresaId);
        usuario.setNome("Usuário");
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setStatus(status);
        usuario.setPrimaryMaster(primaryMaster);
        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(role);
        return usuario;
    }

    private Usuario usuarioExistente(UUID empresaId, UUID onboardingId, String email) {
        Usuario usuario = usuario(empresaId, RoleUsuario.MASTER, true, StatusUsuario.ATIVO);
        usuario.setOnboardingId(onboardingId);
        usuario.setNome("Master");
        usuario.setEmail(email);
        return usuario;
    }
}
