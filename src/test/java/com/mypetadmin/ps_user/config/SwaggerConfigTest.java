package com.mypetadmin.ps_user.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void deveConfigurarMetadadosDaApi() {
        var openApi = new SwaggerConfig().psUserOpenAPI();
        assertThat(openApi.getInfo().getTitle()).isEqualTo("My Pet Admin — PS_User API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openApi.getInfo().getContact().getName()).isEqualTo("My Pet Admin");
    }
}
