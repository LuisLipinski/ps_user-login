package com.mypetadmin.ps_user.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionControllerTest {

    @Test
    void deveRetornarCommitConfigurado() {
        VersionController controller = new VersionController("abc123");

        assertThat(controller.version()).isEqualTo("abc123");
    }
}
