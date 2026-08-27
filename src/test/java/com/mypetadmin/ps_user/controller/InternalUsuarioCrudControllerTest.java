package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.config.CorrelationIdFilter;
import com.mypetadmin.ps_user.dto.PageResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.GlobalExceptionHandler;
import com.mypetadmin.ps_user.security.InternalRequestFilter;
import com.mypetadmin.ps_user.security.SecurityConfig;
import com.mypetadmin.ps_user.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalUsuarioController.class)
@Import({SecurityConfig.class, InternalRequestFilter.class, CorrelationIdFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "security.internal-key=test-key",
        "clients.ps-empresa.url=http://localhost:8081"
})
class InternalUsuarioCrudControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void deveBuscarUsuario() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        when(usuarioService.buscarUsuario(actorId, usuarioId)).thenReturn(response(usuarioId));

        mockMvc.perform(get("/internal/usuarios/{id}", usuarioId)
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId.toString()));
    }

    @Test
    void deveListarUsuariosComPaginacao() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        when(usuarioService.listarUsuarios(eq(actorId), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("nome"), eq("asc")))
                .thenReturn(new PageResponseDTO<>(List.of(response(usuarioId)), 0, 20, 1, 1));

        mockMvc.perform(get("/internal/usuarios")
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(usuarioId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deveAtualizarDadosStatusERole() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        when(usuarioService.atualizarUsuario(eq(actorId), eq(usuarioId), any())).thenReturn(response(usuarioId));
        when(usuarioService.atualizarStatus(eq(actorId), eq(usuarioId), any())).thenReturn(response(usuarioId));
        when(usuarioService.atualizarRole(eq(actorId), eq(usuarioId), any())).thenReturn(response(usuarioId));

        mockMvc.perform(patch("/internal/usuarios/{id}", usuarioId)
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo Nome\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/internal/usuarios/{id}/status", usuarioId)
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/internal/usuarios/{id}/role", usuarioId)
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"BANHO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarRoleInvalidaComoBadRequest() throws Exception {
        mockMvc.perform(patch("/internal/usuarios/{id}/role", UUID.randomUUID())
                        .header("X-Internal-Key", "test-key")
                        .header("X-Actor-User-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_BAD_REQUEST"));
    }

    private UsuarioResponseDTO response(UUID id) {
        return new UsuarioResponseDTO(
                id,
                UUID.randomUUID(),
                "Usuário",
                "user@example.com",
                StatusUsuario.ATIVO,
                false,
                Set.of(RoleUsuario.LOJA),
                OffsetDateTime.now(),
                OffsetDateTime.now());
    }
}
