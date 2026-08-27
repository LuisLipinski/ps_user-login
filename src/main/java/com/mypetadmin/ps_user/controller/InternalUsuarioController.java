package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> excluirUsuario(
            @RequestHeader(ACTOR_USER_ID_HEADER) UUID actorUserId,
            @PathVariable UUID usuarioId) {
        usuarioService.excluirUsuario(actorUserId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
