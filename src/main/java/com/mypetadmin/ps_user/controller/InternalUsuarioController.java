package com.mypetadmin.ps_user.controller;

import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/usuarios")
@RequiredArgsConstructor
public class InternalUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/master")
    public ResponseEntity<UsuarioResponseDTO> criarMaster(@Valid @RequestBody UsuarioMasterCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarMaster(request));
    }
}
