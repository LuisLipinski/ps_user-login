package com.mypetadmin.ps_user.dto;

import com.mypetadmin.ps_user.enums.RoleUsuario;
import jakarta.validation.constraints.NotNull;

public record UsuarioRoleUpdateRequestDTO(
        @NotNull RoleUsuario role
) {
}
