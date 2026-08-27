package com.mypetadmin.ps_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioMasterCreateRequestDTO(
        @NotNull UUID empresaId,
        @NotNull UUID onboardingId,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 320) String email
) {
}
