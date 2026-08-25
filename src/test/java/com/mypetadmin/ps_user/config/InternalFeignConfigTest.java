package com.mypetadmin.ps_user.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalFeignConfigTest {

    private final InternalFeignConfig config = new InternalFeignConfig();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void devePropagarChaveInternaECorrelationId() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-123");
        RequestInterceptor interceptor = config.internalKeyInterceptor("internal-secret");
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get("X-Internal-Key")).containsExactly("internal-secret");
        assertThat(template.headers().get(CorrelationIdFilter.HEADER_NAME)).containsExactly("corr-123");
    }

    @Test
    void deveFalharSemChaveInterna() {
        assertThatThrownBy(() -> config.internalKeyInterceptor(" "))
                .isInstanceOf(IllegalStateException.class);
    }
}
