package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.config.CorrelationIdFilter;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.GlobalExceptionHandler;
import com.mypetadmin.ps_user.exception.UsuarioOperacaoNaoPermitidaException;
import com.mypetadmin.ps_user.security.InternalRequestFilter;
import com.mypetadmin.ps_user.security.SecurityConfig;
import com.mypetadmin.ps_user.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalUsuarioController.class)
@Import({SecurityConfig.class, InternalRequestFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "security.internal-key=test-key",
        "clients.ps-empresa.url=http://localhost:8081"
})
class InternalUsuarioControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void deveRejeitarEndpointInternoSemChave() throws Exception {
        mockMvc.perform(post("/internal/usuarios/master")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMasterBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    void deveRejeitarChaveInternaInvalida() throws Exception {
        mockMvc.perform(post("/internal/usuarios/master")
                        .header("X-Internal-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMasterBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarMasterComChaveInternaValidaECorrelationId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        when(usuarioService.criarMaster(any())).thenReturn(response(userId, empresaId, RoleUsuario.MASTER, true));

        mockMvc.perform(post("/internal/usuarios/master")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMasterBody(empresaId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "corr-123"))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.primaryMaster").value(true))
                .andExpect(jsonPath("$.roles[0]").value("MASTER"));
    }

    @Test
    void deveCriarUsuarioGerenciadoComActorInterno() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        when(usuarioService.criarUsuario(any(), any())).thenReturn(response(userId, empresaId, RoleUsuario.LOJA, false));

        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Loja\",\"email\":\"loja@example.com\",\"role\":\"LOJA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.primaryMaster").value(false))
                .andExpect(jsonPath("$.roles[0]").value("LOJA"));
    }

    @Test
    void deveExcluirUsuarioGerenciadoComActorInterno() throws Exception {
        mockMvc.perform(delete("/internal/usuarios/{id}", UUID.randomUUID())
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveExigirActorNasOperacoesGerenciadas() throws Exception {
        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Loja\",\"email\":\"loja@example.com\",\"role\":\"LOJA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_BAD_REQUEST"));
    }

    @Test
    void deveMapearRoleInvalidaComoBadRequest() throws Exception {
        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Loja\",\"email\":\"loja@example.com\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_BAD_REQUEST"));
    }

    @Test
    void deveMapearPermissaoNegadaComo403() throws Exception {
        when(usuarioService.criarUsuario(any(), any())).thenThrow(new UsuarioOperacaoNaoPermitidaException());

        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Admin\",\"email\":\"admin@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_OPERATION_FORBIDDEN"));
    }

    @Test
    void deveMapearValidacaoComo400() throws Exception {
        mockMvc.perform(post("/internal/usuarios/master")
                        .header("X-Internal-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_VALIDATION_ERROR"));
    }

    @Test
    void deveNegarRotaForaDoContratoMesmoAutenticada() throws Exception {
        mockMvc.perform(get("/fora-do-contrato").header("X-Internal-Key", "test-key"))
                .andExpect(status().isForbidden());
    }

    @Test
    void probeDaRaizContinuaBloqueado() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    private UsuarioResponseDTO response(UUID userId, UUID empresaId, RoleUsuario role, boolean primaryMaster) {
        return new UsuarioResponseDTO(
                userId,
                empresaId,
                "Usuário",
                "usuario@example.com",
                StatusUsuario.ATIVO,
                primaryMaster,
                Set.of(role),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    private String validMasterBody() {
        return validMasterBody(UUID.randomUUID());
    }

    private String validMasterBody(UUID empresaId) {
        return "{\"empresaId\":\"" + empresaId + "\",\"onboardingId\":\"" + UUID.randomUUID()
                + "\",\"nome\":\"Master\",\"email\":\"master@example.com\"}";
    }
}
