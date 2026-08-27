package com.mypetadmin.ps_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequestDTO(
        @Size(max = 150) String nome,
        @Email @Size(max = 320) String email
) {
}
