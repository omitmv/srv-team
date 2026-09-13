package com.example.srvteam.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.srvteam.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
  http.csrf(csrf -> csrf.disable())
    .exceptionHandling(ex -> ex.authenticationEntryPoint(
        (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(authz -> authz
      // Endpoints públicos (não precisam de autenticação)
      .requestMatchers("/v1/usuario/login").permitAll()
      .requestMatchers(HttpMethod.POST, "/v1/usuario").permitAll() // Criação de usuário
      .requestMatchers("/actuator/**").permitAll()
      .requestMatchers(HttpMethod.POST, "/v1/automacao").permitAll() // Endpoint para automação
      .requestMatchers("/v1/pontuacao-hist/resumoCompeticao/**").permitAll() // Console H2

      // Todos os outros endpoints precisam de autenticação
      .anyRequest().authenticated())
    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // Para o console H2
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

  return http.build();
  }
}
