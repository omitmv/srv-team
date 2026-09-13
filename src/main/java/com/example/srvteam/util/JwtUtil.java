package com.example.srvteam.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

  private final String secret;

  private final Long expiration;

  public JwtUtil(@Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration:86400000}") Long expiration) {
    Assert.hasText(secret, "jwt.secret must be configured");
    Assert.isTrue(secret.getBytes(StandardCharsets.UTF_8).length >= 32,
        "jwt.secret must contain at least 32 bytes");
    this.secret = secret;
    this.expiration = expiration;
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Gera um token JWT para o usuário
   */
  public String generateToken(String login, Integer cdUsuario, String nome) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("cdUsuario", cdUsuario);
    claims.put("nome", nome);
    return createToken(claims, login);
  }

  /**
   * Cria o token JWT
   */
  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Extrai o login (subject) do token
   */
  public String extractLogin(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrai o código do usuário do token
   */
  public Integer extractCdUsuario(String token) {
    return extractClaim(token, claims -> claims.get("cdUsuario", Integer.class));
  }

  /**
   * Extrai o nome do usuário do token
   */
  public String extractNome(String token) {
    return extractClaim(token, claims -> claims.get("nome", String.class));
  }

  /**
   * Extrai a data de expiração do token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrai uma claim específica do token
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extrai todas as claims do token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Verifica se o token expirou
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Valida o token
   */
  public Boolean validateToken(String token, String login) {
    final String extractedLogin = extractLogin(token);
    return (extractedLogin.equals(login) && !isTokenExpired(token));
  }

  /**
   * Valida se o token é válido (sem verificar usuário específico)
   */
  public Boolean isTokenValid(String token) {
    try {
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }
}
