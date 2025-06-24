package com.petshop.login.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message = "Senha antiga não pode estar em branco")
    private String oldPassword;

    @NotBlank(message = "Nova senha não pode estar em branco")
    @Size(min = 8, max = 20, message = "A nova senha deve ter entre 8 e 20 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "A nova senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um dígito e um caractere especial"
    )
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}