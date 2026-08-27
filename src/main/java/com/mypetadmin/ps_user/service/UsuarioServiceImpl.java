package com.mypetadmin.ps_user.service;

import com.mypetadmin.ps_user.client.EmpresaClient;
import com.mypetadmin.ps_user.dto.EmpresaStatusResponseDTO;
import com.mypetadmin.ps_user.dto.UsuarioCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioMasterCreateRequestDTO;
import com.mypetadmin.ps_user.dto.UsuarioResponseDTO;
import com.mypetadmin.ps_user.entity.Usuario;
import com.mypetadmin.ps_user.enums.RoleUsuario;
import com.mypetadmin.ps_user.enums.StatusUsuario;
import com.mypetadmin.ps_user.exception.EmailExistenteException;
import com.mypetadmin.ps_user.exception.EmpresaIndisponivelException;
import com.mypetadmin.ps_user.exception.EmpresaNaoEncontradaException;
import com.mypetadmin.ps_user.exception.OnboardingConflictException;
import com.mypetadmin.ps_user.exception.PrimaryMasterExistenteException;
import com.mypetadmin.ps_user.exception.PrimeiroMasterProtegidoException;
import com.mypetadmin.ps_user.exception.UsuarioNaoEncontradoException;
import com.mypetadmin.ps_user.exception.UsuarioOperacaoNaoPermitidaException;
import com.mypetadmin.ps_user.mapper.UsuarioMapper;
import com.mypetadmin.ps_user.repository.UsuarioRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<RoleUsuario> ROLES_OPERACIONAIS = EnumSet.of(
            RoleUsuario.LOJA,
            RoleUsuario.VETERINARIO,
            RoleUsuario.BANHO,
            RoleUsuario.HOTEL,
            RoleUsuario.CRECHE
    );

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final EmpresaClient empresaClient;

    @Override
    @Transactional
    public UsuarioResponseDTO criarMaster(UsuarioMasterCreateRequestDTO request) {
        String emailNormalizado = normalizeEmail(request.email());

        var existente = usuarioRepository.findByOnboardingId(request.onboardingId());
        if (existente.isPresent()) {
            return validarReplay(existente.get(), request, emailNormalizado);
        }

        validarEmpresa(request.empresaId());

        if (usuarioRepository.existsByEmpresaIdAndPrimaryMasterTrue(request.empresaId())) {
            throw new PrimaryMasterExistenteException();
        }

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailExistenteException();
        }

        Usuario usuario = novoUsuario(request.empresaId(), request.nome(), emailNormalizado, RoleUsuario.MASTER);
        usuario.setOnboardingId(request.onboardingId());
        usuario.setPrimaryMaster(true);

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            log.info("user.master.created userId={} empresaId={} onboardingId={}",
                    salvo.getId(), salvo.getEmpresaId(), salvo.getOnboardingId());
            return usuarioMapper.toResponse(salvo);
        } catch (DataIntegrityViolationException ex) {
            var concorrente = usuarioRepository.findByOnboardingId(request.onboardingId());
            if (concorrente.isPresent()) {
                return validarReplay(concorrente.get(), request, emailNormalizado);
            }
            if (usuarioRepository.existsByEmpresaIdAndPrimaryMasterTrue(request.empresaId())) {
                throw new PrimaryMasterExistenteException();
            }
            log.warn("user.master.conflict empresaId={} onboardingId={}", request.empresaId(), request.onboardingId());
            throw new EmailExistenteException();
        }
    }

    @Override
    @Transactional
    public UsuarioResponseDTO criarUsuario(UUID actorUserId, UsuarioCreateRequestDTO request) {
        Usuario actor = carregarActorAtivo(actorUserId);
        RoleUsuario actorRole = roleUnica(actor);
        validarRolePermitidaParaCriacao(actorRole, request.role());

        String emailNormalizado = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailExistenteException();
        }

        Usuario usuario = novoUsuario(actor.getEmpresaId(), request.nome(), emailNormalizado, request.role());
        usuario.setPrimaryMaster(false);

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            log.info("user.created actorUserId={} userId={} empresaId={} role={}",
                    actor.getId(), salvo.getId(), salvo.getEmpresaId(), request.role());
            return usuarioMapper.toResponse(salvo);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailExistenteException();
        }
    }

    @Override
    @Transactional
    public void excluirUsuario(UUID actorUserId, UUID usuarioId) {
        Usuario actor = carregarActorAtivo(actorUserId);
        Usuario alvo = usuarioRepository.findByIdAndEmpresaId(usuarioId, actor.getEmpresaId())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if (alvo.isPrimaryMaster()) {
            throw new PrimeiroMasterProtegidoException();
        }

        RoleUsuario actorRole = roleUnica(actor);
        RoleUsuario alvoRole = roleUnica(alvo);
        validarPodeExcluir(actorRole, alvoRole);

        usuarioRepository.delete(alvo);
        log.info("user.deleted actorUserId={} userId={} empresaId={} targetRole={}",
                actor.getId(), alvo.getId(), alvo.getEmpresaId(), alvoRole);
    }

    private Usuario novoUsuario(UUID empresaId, String nome, String emailNormalizado, RoleUsuario role) {
        Usuario usuario = new Usuario();
        usuario.setEmpresaId(empresaId);
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(role);
        return usuario;
    }

    private Usuario carregarActorAtivo(UUID actorUserId) {
        Usuario actor = usuarioRepository.findById(actorUserId)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        if (actor.getStatus() != StatusUsuario.ATIVO) {
            throw new UsuarioOperacaoNaoPermitidaException();
        }
        return actor;
    }

    private void validarRolePermitidaParaCriacao(RoleUsuario actorRole, RoleUsuario novaRole) {
        if (actorRole == RoleUsuario.MASTER) {
            return;
        }
        if (actorRole == RoleUsuario.ADMIN && ROLES_OPERACIONAIS.contains(novaRole)) {
            return;
        }
        throw new UsuarioOperacaoNaoPermitidaException();
    }

    private void validarPodeExcluir(RoleUsuario actorRole, RoleUsuario alvoRole) {
        if (actorRole == RoleUsuario.MASTER) {
            return;
        }
        if (actorRole == RoleUsuario.ADMIN && ROLES_OPERACIONAIS.contains(alvoRole)) {
            return;
        }
        throw new UsuarioOperacaoNaoPermitidaException();
    }

    private RoleUsuario roleUnica(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().size() != 1) {
            throw new UsuarioOperacaoNaoPermitidaException();
        }
        return usuario.getRoles().iterator().next();
    }

    private UsuarioResponseDTO validarReplay(Usuario usuario,
                                              UsuarioMasterCreateRequestDTO request,
                                              String emailNormalizado) {
        if (!usuario.getEmpresaId().equals(request.empresaId())
                || !usuario.getEmail().equalsIgnoreCase(emailNormalizado)) {
            throw new OnboardingConflictException();
        }
        return usuarioMapper.toResponse(usuario);
    }

    private void validarEmpresa(UUID empresaId) {
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
