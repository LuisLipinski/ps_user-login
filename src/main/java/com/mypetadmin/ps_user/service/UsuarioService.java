package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;

public interface UsuarioService {
    UsuarioResponseDTO criarMaster(UsuarioMasterCreateRequestDTO request);
}
