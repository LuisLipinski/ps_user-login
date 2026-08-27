package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.dto.PageResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioIdentityResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioRoleUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioStatusUpdateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioUpdateRequestDTO;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;

import java.util.UUID;

public interface UsuarioService {
    UsuarioResponseDTO criarMaster(UsuarioMasterCreateRequestDTO request);

    UsuarioResponseDTO criarUsuario(UUID actorUserId, UsuarioCreateRequestDTO request);

    UsuarioIdentityResponseDTO buscarIdentidadePorEmail(String email);

    UsuarioResponseDTO buscarUsuario(UUID actorUserId, UUID usuarioId);

    PageResponseDTO<UsuarioResponseDTO> listarUsuarios(
            UUID actorUserId,
            String nome,
            String email,
            StatusUsuario status,
            RoleUsuario role,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    UsuarioResponseDTO atualizarUsuario(UUID actorUserId, UUID usuarioId, UsuarioUpdateRequestDTO request);

    UsuarioResponseDTO atualizarStatus(UUID actorUserId, UUID usuarioId, UsuarioStatusUpdateRequestDTO request);

    UsuarioResponseDTO atualizarRole(UUID actorUserId, UUID usuarioId, UsuarioRoleUpdateRequestDTO request);

    void excluirUsuario(UUID actorUserId, UUID usuarioId);
}
