package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;

import java.util.UUID;

public interface UsuarioService {
    UsuarioResponseDTO criarMaster(UsuarioMasterCreateRequestDTO request);

    UsuarioResponseDTO criarUsuario(UUID actorUserId, UsuarioCreateRequestDTO request);

    void excluirUsuario(UUID actorUserId, UUID usuarioId);
}
