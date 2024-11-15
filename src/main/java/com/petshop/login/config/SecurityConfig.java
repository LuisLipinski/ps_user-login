package com.petshop.login.config;

import com.petshop.login.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.inMemoryAuthentication()
                .withUser("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER");
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/usuario/forgot-password").permitAll()
                        .requestMatchers("/usuario/reset-password").permitAll()
                        .requestMatchers("/login/").permitAll()
                        .requestMatchers("/usuario/register").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/edit/{id}").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/edit/{id}").permitAll()
                        .requestMatchers("/usuario/delete/{id}").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/all").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/{id}").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/nome").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/email").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/usuario/role").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
