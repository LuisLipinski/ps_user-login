package com.mypetadmin.ps_user.dto;

import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;

import java.util.Set;
import java.util.UUID;

public record UsuarioIdentityResponseDTO(
        UUID userId,
        UUID empresaId,
        String email,
        StatusUsuario status,
        Set<RoleUsuario> roles
) {
}
