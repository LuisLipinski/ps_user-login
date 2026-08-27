package com.mypetadmin.ps_user.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void deveNormalizarEmailEDefinirTimestampsAoPersistir() {
        Usuario usuario = new Usuario();
        usuario.setEmail("  MASTER@EXAMPLE.COM  ");

        usuario.prePersist();

        assertThat(usuario.getEmail()).isEqualTo("master@example.com");
        assertThat(usuario.getDataCriacao()).isNotNull();
        assertThat(usuario.getDataAtualizacao()).isEqualTo(usuario.getDataCriacao());
    }

    @Test
    void deveAtualizarTimestampENormalizarEmailAoAtualizar() {
        Usuario usuario = new Usuario();
        usuario.setEmail("master@example.com");
        usuario.prePersist();
        OffsetDateTime anterior = usuario.getDataAtualizacao();
        usuario.setEmail(" NOVO@EXAMPLE.COM ");

        usuario.preUpdate();

        assertThat(usuario.getEmail()).isEqualTo("novo@example.com");
        assertThat(usuario.getDataAtualizacao()).isAfterOrEqualTo(anterior);
    }

    @Test
    void deveAceitarEmailNuloNoLifecycleSemFalhar() {
        Usuario usuario = new Usuario();
        usuario.prePersist();
        usuario.preUpdate();
        assertThat(usuario.getEmail()).isNull();
    }
}
