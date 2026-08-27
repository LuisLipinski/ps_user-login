package com.mypetadmin.ps_user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void devePropagarCorrelationIdRecebidoELimparMdc() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "corr-123");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("corr-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveGerarCorrelationIdQuandoAusenteOuEmBranco() throws Exception {
        for (String header : new String[]{null, "   "}) {
            var request = new MockHttpServletRequest();
            if (header != null) request.addHeader(CorrelationIdFilter.HEADER_NAME, header);
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, mock(FilterChain.class));
            assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isNotBlank();
            assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
        }
    }

    @Test
    void deveLimparMdcMesmoQuandoCadeiaFalha() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new ServletException("falha")).when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, chain)).isInstanceOf(ServletException.class);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
