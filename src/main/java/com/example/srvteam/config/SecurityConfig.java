package com.example.srvteam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.srvteam.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  private CorsConfigurationSource corsConfigurationSource;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            // Endpoints públicos (não precisam de autenticação)
            .requestMatchers("/v1/usuario/login").permitAll()
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers("/v1/tipo-acesso").permitAll()
            .requestMatchers("POST", "/v1/usuario").permitAll() // Criação de usuário
            .requestMatchers("/actuator/**").permitAll()

            // Todos os outros endpoints precisam de autenticação
            .anyRequest().authenticated())
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // Para o console H2
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
