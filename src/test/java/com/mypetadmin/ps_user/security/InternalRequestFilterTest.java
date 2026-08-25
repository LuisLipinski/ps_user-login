package com.mypetadmin.ps_user.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InternalRequestFilterTest {

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarChaveInternaValida() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(InternalRequestFilter.INTERNAL_KEY_HEADER, "secret");
        new InternalRequestFilter("secret").doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("internal-service");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_INTERNAL");
    }

    @Test
    void naoDeveAutenticarChaveInvalidaAusenteOuConfiguracaoVazia() throws Exception {
        executar(new InternalRequestFilter("secret"), null);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        executar(new InternalRequestFilter("secret"), "wrong");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        executar(new InternalRequestFilter(""), "secret");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void naoDeveSobrescreverAutenticacaoExistente() throws Exception {
        var existente = new TestingAuthenticationToken("existing", null);
        SecurityContextHolder.getContext().setAuthentication(existente);
        executar(new InternalRequestFilter("secret"), "secret");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existente);
    }

    private void executar(InternalRequestFilter filter, String key) throws Exception {
        var request = new MockHttpServletRequest();
        if (key != null) request.addHeader(InternalRequestFilter.INTERNAL_KEY_HEADER, key);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
    }
}
