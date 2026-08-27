package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.dto.PageResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioRoleUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioStatusUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioUpdateRequestDTO;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/usuarios")
@RequiredArgsConstructor
public class InternalUsuarioController {

    private static final String ACTOR_USER_ID_HEADER = "X-Actor-User-Id";

    private final UsuarioService usuarioService;

    @PostMapping("/master")
    public ResponseEntity<UsuarioResponseDTO> criarMaster(@Valid @RequestBody UsuarioMasterCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarMaster(request));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @Valid @RequestBody UsuarioCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarUsuario(actorUserId, request));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuario(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarioService.buscarUsuario(actorUserId, usuarioId));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listarUsuarios(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) StatusUsuario status,
            @RequestParam(required = false) RoleUsuario role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(
                actorUserId, nome, email, status, role, page, size, sortBy, sortDirection));
    }

    @PatchMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody UsuarioUpdateRequestDTO request) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(actorUserId, usuarioId, request));
    }

    @PatchMapping("/{usuarioId}/status")
    public ResponseEntity<UsuarioResponseDTO> atualizarStatus(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody UsuarioStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(usuarioService.atualizarStatus(actorUserId, usuarioId, request));
    }

    @PatchMapping("/{usuarioId}/role")
    public ResponseEntity<UsuarioResponseDTO> atualizarRole(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody UsuarioRoleUpdateRequestDTO request) {
        return ResponseEntity.ok(usuarioService.atualizarRole(actorUserId, usuarioId, request));
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> excluirUsuario(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId) {
        usuarioService.excluirUsuario(actorUserId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
