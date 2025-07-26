package com.example.srvteam.filter;

import com.example.srvteam.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authorizationHeader = request.getHeader("Authorization");

    String login = null;
    String jwt = null;

    // Verifica se o header Authorization existe e começa com "Bearer "
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      jwt = authorizationHeader.substring(7); // Remove "Bearer " do início
      try {
        login = jwtUtil.extractLogin(jwt);
      } catch (Exception e) {
        logger.error("Erro ao extrair login do token JWT: " + e.getMessage());
      }
    }

    // Se o token é válido e não há autenticação no contexto
    if (login != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      if (jwtUtil.isTokenValid(jwt)) {
        // Cria um UserDetails simples para o Spring Security
        UserDetails userDetails = User.builder()
            .username(login)
            .password("") // Não precisamos da senha aqui
            .authorities(new ArrayList<>()) // Sem roles por enquanto
            .build();

        // Cria o token de autenticação do Spring Security
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Define a autenticação no contexto do Spring Security
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Adiciona informações do usuário nos headers da resposta (opcional)
        response.setHeader("X-User-Login", login);
        response.setHeader("X-User-Id", String.valueOf(jwtUtil.extractCdUsuario(jwt)));
        response.setHeader("X-User-Name", jwtUtil.extractNome(jwt));
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();

    // Endpoints que não precisam de autenticação
    return path.equals("/v1/usuarios/login") ||
        (path.equals("/v1/usuarios") && "POST".equals(request.getMethod())) ||
        path.startsWith("/h2-console") ||
        path.startsWith("/actuator") ||
        path.equals("/favicon.ico");
  }
}
