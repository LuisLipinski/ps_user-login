package com.mypetadmin.ps_user.dto;

import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        UUID empresaId,
        String nome,
        String email,
        StatusUsuario status,
        Set<RoleUsuario> roles,
        OffsetDateTime dataCriacao,
        OffsetDateTime dataAtualizacao
) {
}
