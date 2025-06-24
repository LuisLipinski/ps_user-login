package com.petshop.login.util;

import com.petshop.login.model.NivelAcesso;
import org.springframework.stereotype.Component;

@Component
public class UserRoleHierarchy {

    public boolean canAssignRole(NivelAcesso assignerRole, NivelAcesso requestedRole) {
        if (assignerRole == NivelAcesso.MASTER) {
            return true;
        }
        if (assignerRole == NivelAcesso.ADMIN) {
            return requestedRole != NivelAcesso.MASTER && requestedRole != NivelAcesso.ADMIN;
        }
        return false; // Outras roles não podem criar usuários
    }

    public boolean canUpdateRole(NivelAcesso updaterRole, NivelAcesso targetRole, NivelAcesso newRole) {
        if (updaterRole == NivelAcesso.MASTER) {
            return true;
        }
        if (updaterRole == NivelAcesso.ADMIN) {
            return targetRole != NivelAcesso.MASTER; // Admin não pode editar Master
        }
        return false; // Outras roles não podem editar roles
    }

    public boolean canDeleteUser(NivelAcesso deleterRole, NivelAcesso targetRole) {
        if (deleterRole == NivelAcesso.MASTER) {
            return true;
        }
        if (deleterRole == NivelAcesso.ADMIN) {
            return targetRole != NivelAcesso.MASTER && targetRole != NivelAcesso.ADMIN;
        }
        return false; // Outras roles não podem deletar usuários
    }

    // Você pode adicionar mais métodos para outras permissões, se necessário
}