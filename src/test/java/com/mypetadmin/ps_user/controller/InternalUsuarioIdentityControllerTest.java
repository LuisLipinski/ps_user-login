package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.config.CorrelationIdFilter;
import com.mypetadmin.ps_user.dto.UsuarioIdentityResponseDTO;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalUsuarioController.class)
@Import({SecurityConfig.class, InternalRequestFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "security.internal-key=test-key",
        "clients.ps-empresa.url=http://localhost:8081"
})
class InternalUsuarioIdentityControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void deveExporIdentidadeSomenteComChaveInternaSemActorHeader() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        when(usuarioService.buscarIdentidadePorEmail("master@example.com")).thenReturn(
                new UsuarioIdentityResponseDTO(userId, empresaId, "master@example.com",
                        StatusUsuario.ATIVO, Set.of(RoleUsuario.MASTER)));

        mockMvc.perform(get("/internal/usuarios/identity")
                        .header("X-Internal-Key", "test-key")
                        .param("email", "master@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.empresaId").value(empresaId.toString()))
                .andExpect(jsonPath("$.roles[0]").value("MASTER"));
    }

    @Test
    void deveRejeitarIdentidadeSemChaveInterna() throws Exception {
        mockMvc.perform(get("/internal/usuarios/identity")
                        .param("email", "master@example.com"))
                .andExpect(status().isUnauthorized());
    }
}
