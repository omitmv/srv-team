package com.example.srvteam.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKey12345678901234567890123456789012345678901234567890");
    ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
  }

  @Test
  void testGenerateToken() {
    String token = jwtUtil.generateToken("testuser", 1, "Test User");
    assertNotNull(token);
    assertTrue(token.length() > 0);
  }

  @Test
  void testExtractLogin() {
    String token = jwtUtil.generateToken("testuser", 1, "Test User");
    String login = jwtUtil.extractLogin(token);
    assertEquals("testuser", login);
  }

  @Test
  void testExtractCdUsuario() {
    String token = jwtUtil.generateToken("testuser", 123, "Test User");
    Integer cdUsuario = jwtUtil.extractCdUsuario(token);
    assertEquals(123, cdUsuario);
  }

  @Test
  void testExtractNome() {
    String token = jwtUtil.generateToken("testuser", 1, "João Silva");
    String nome = jwtUtil.extractNome(token);
    assertEquals("João Silva", nome);
  }

  @Test
  void testValidateToken() {
    String token = jwtUtil.generateToken("testuser", 1, "Test User");
    Boolean isValid = jwtUtil.validateToken(token, "testuser");
    assertTrue(isValid);
  }

  @Test
  void testValidateTokenWithWrongUser() {
    String token = jwtUtil.generateToken("testuser", 1, "Test User");
    Boolean isValid = jwtUtil.validateToken(token, "wronguser");
    assertFalse(isValid);
  }

  @Test
  void testIsTokenValid() {
    String token = jwtUtil.generateToken("testuser", 1, "Test User");
    Boolean isValid = jwtUtil.isTokenValid(token);
    assertTrue(isValid);
  }

  @Test
  void testIsTokenValidWithInvalidToken() {
    Boolean isValid = jwtUtil.isTokenValid("invalid.token.here");
    assertFalse(isValid);
  }
}
