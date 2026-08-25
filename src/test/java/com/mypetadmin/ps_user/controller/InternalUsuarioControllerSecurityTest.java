package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.config.CorrelationIdFilter;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalUsuarioController.class)
@Import({SecurityConfig.class, InternalRequestFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.internal-key=test-key")
class InternalUsuarioControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void deveRejeitarEndpointInternoSemChave() throws Exception {
        mockMvc.perform(post("/internal/usuarios/master")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    void deveRejeitarChaveInternaInvalida() throws Exception {
        mockMvc.perform(post("/internal/usuarios/master")
                        .header("X-Internal-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarMasterComChaveInternaValidaECorrelationId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        when(usuarioService.criarMaster(any())).thenReturn(new UsuarioResponseDTO(
                userId, empresaId, "Master", "master@example.com", StatusUsuario.ATIVO,
                Set.of(RoleUsuario.MASTER), OffsetDateTime.now(), OffsetDateTime.now()));

        mockMvc.perform(post("/internal/usuarios/master")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(empresaId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "corr-123"))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.roles[0]").value("MASTER"));
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

    private String validBody() {
        return validBody(UUID.randomUUID());
    }

    private String validBody(UUID empresaId) {
        return "{\"empresaId\":\"" + empresaId + "\",\"onboardingId\":\"" + UUID.randomUUID()
                + "\",\"nome\":\"Master\",\"email\":\"master@example.com\"}";
    }
}
