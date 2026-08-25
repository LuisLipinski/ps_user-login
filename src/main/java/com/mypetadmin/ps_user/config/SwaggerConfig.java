package com.mypetadmin.ps_user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI psUserOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("My Pet Admin — PS_User API")
                .description("API do microsserviço de usuários do My Pet Admin")
                .version("v1")
                .contact(new Contact().name("My Pet Admin")));
    }
}
