package com.mypetadmin.ps_user.mapper;

import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmpresaId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getStatus(),
                Set.copyOf(usuario.getRoles()),
                usuario.getDataCriacao(),
                usuario.getDataAtualizacao()
        );
    }
}
