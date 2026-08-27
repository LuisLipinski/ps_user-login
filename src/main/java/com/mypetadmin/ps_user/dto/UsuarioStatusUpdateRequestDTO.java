package com.mypetadmin.ps_user.dto;

import com.mypetadmin.ps_user.enums.StatusUsuario;
import jakarta.validation.constraints.NotNull;

public record UsuarioStatusUpdateRequestDTO(
        @NotNull StatusUsuario status
) {
}
