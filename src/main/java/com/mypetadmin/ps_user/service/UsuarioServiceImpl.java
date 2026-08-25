package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.dto.EmpresaStatusResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.EmailExistenteException;
import com.mypetadmin.ps_user.exception.EmpresaIndisponivelException;
import com.mypetadmin.ps_user.exception.EmpresaNaoEncontradaException;
import com.mypetadmin.ps_user.exception.OnboardingConflictException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final EmpresaClient empresaClient;

    @Override
    @Transactional
    public UsuarioResponseDTO criarMaster(UsuarioMasterCreateRequestDTO request) {
        String emailNormalizado = normalizeEmail(request.email());

        var existente = usuarioRepository.findByOnboardingId(request.onboardingId());
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (!usuario.getEmpresaId().equals(request.empresaId()) || !usuario.getEmail().equalsIgnoreCase(emailNormalizado)) {
                throw new OnboardingConflictException();
            }
            return usuarioMapper.toResponse(usuario);
        }

        validarEmpresa(request.empresaId());

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailExistenteException();
        }

        Usuario usuario = new Usuario();
        usuario.setEmpresaId(request.empresaId());
        usuario.setOnboardingId(request.onboardingId());
        usuario.setNome(request.nome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(RoleUsuario.MASTER);

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            log.info("user.master.created userId={} empresaId={} onboardingId={}",
                    salvo.getId(), salvo.getEmpresaId(), salvo.getOnboardingId());
            return usuarioMapper.toResponse(salvo);
        } catch (DataIntegrityViolationException ex) {
            log.warn("user.master.conflict empresaId={} onboardingId={}", request.empresaId(), request.onboardingId());
            throw new EmailExistenteException();
        }
    }

    private void validarEmpresa(java.util.UUID empresaId) {
        try {
            EmpresaStatusResponseDTO empresa = empresaClient.buscarStatusEmpresa(empresaId);
            if (empresa == null || empresa.empresaId() == null || !empresaId.equals(empresa.empresaId())) {
                throw new EmpresaNaoEncontradaException();
            }
        } catch (FeignException.NotFound ex) {
            throw new EmpresaNaoEncontradaException();
        } catch (FeignException ex) {
            log.warn("integration.ps_empresa.failed status={} empresaId={}", ex.status(), empresaId);
            throw new EmpresaIndisponivelException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
