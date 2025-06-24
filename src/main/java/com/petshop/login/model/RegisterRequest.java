package com.petshop.login.model;

import com.petshop.login.model.NivelAcesso;
import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 4, max = 16, message = "O nome deve ter entre 4 e 16 caracteres")
    private String nome;

    @NotBlank(message = "Email não pode estar em branco")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Senha não pode estar em branco")
    @Size(min = 8, max = 20, message = "A senha deve ter entre 8 e 20 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um dígito e um caractere especial"
    )
    private String senha;

    @NotNull(message = "Nível de acesso não pode ser nulo")
    private NivelAcesso nivelAcesso;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(NivelAcesso nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}