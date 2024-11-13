package com.petshop.login.service;

import com.petshop.login.exception.ValidationException;
import com.petshop.login.model.ChangePasswordRequest;
import com.petshop.login.model.NivelAcesso;
import com.petshop.login.model.RegisterRequest;

import java.util.regex.Pattern;

public class UserValidation {
    private static final String EMAIL_REGEX = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})*$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static void validateRegisterRequest(RegisterRequest registerRequest) {
        validatePassword(registerRequest.getSenha());
        validateEmail(registerRequest.getEmail());
        validateUsername(registerRequest.getNome());
    }

    public static void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            throw new ValidationException("A senha deve ter entre 8 e 20 caracteres.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new ValidationException("A senha deve conter pelo menos um número.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("A senha deve conter pelo menos uma letra minúscula.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("A senha deve conter pelo menos uma letra maiúscula.");
        }
        if (password.matches(".*[(),.\"{}|<>].*")) {
            throw new ValidationException("A senha não deve possuir esses caracteres (),.\"{}|<>].*");
        }
        if (!password.matches(".*[!@#$%^&*?:].*")) {
            throw new ValidationException("A senha deve conter pelo menos um caracter especial");
        }
    }

    public static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Formato de email invalido");
        }
    }

    public static void validateUsername(String username) {
        if (username.length() < 4 || username.length() > 16) {
            throw new ValidationException("O nome de usuário deve ter entre 4 e 16 caracatres.");
        }
        if (username.contains(" ")) {
            throw new ValidationException("O nome de usuário não pode conter espaços.");
        }
    }

    public static void validateNivelAcesso(String nivelAcessoString) {
        if (nivelAcessoString == null || nivelAcessoString.trim().isEmpty()) {
            throw new ValidationException("Nível de acesso não pode ser nulo.");
        }

        try {
            NivelAcesso nivelAcesso = NivelAcesso.valueOf(nivelAcessoString.toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder allowedValues = new StringBuilder();
            for (NivelAcesso nivel : NivelAcesso.values()) {
                allowedValues.append(nivel.name()).append(", ");
            }
            if (allowedValues.length() > 0) {
                allowedValues.setLength(allowedValues.length() - 2);
            }
            throw new ValidationException("Nível de acesso inválido. Níveis permitidos: " + allowedValues.toString());
        }


    }

}
