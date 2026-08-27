package com.mypetadmin.ps_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.mypetadmin.ps_user.client")
public class PsUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsUserApplication.class, args);
    }
}
