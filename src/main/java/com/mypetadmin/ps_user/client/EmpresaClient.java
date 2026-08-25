package com.mypetadmin.ps_user.client;

import com.mypetadmin.ps_user.config.InternalFeignConfig;
import com.mypetadmin.ps_user.dto.EmpresaStatusResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "ps-empresa",
        url = "${clients.ps-empresa.url}",
        configuration = InternalFeignConfig.class
)
public interface EmpresaClient {

    @GetMapping("/internal/empresas/{id}/status")
    EmpresaStatusResponseDTO buscarStatusEmpresa(@PathVariable("id") UUID id);
}
