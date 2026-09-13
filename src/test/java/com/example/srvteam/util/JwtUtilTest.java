package com.example.srvteam.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtUtilTest {

  @Test
  void shouldGenerateAndValidateToken() {
    JwtUtil jwtUtil = new JwtUtil("01234567890123456789012345678901", 60_000L);

    String token = jwtUtil.generateToken("tester", 12, "Tester");

    assertEquals("tester", jwtUtil.extractLogin(token));
    assertEquals(12, jwtUtil.extractCdUsuario(token));
    assertEquals("Tester", jwtUtil.extractNome(token));
    assertTrue(jwtUtil.validateToken(token, "tester"));
  }

  @Test
  void shouldRejectInvalidToken() {
    JwtUtil jwtUtil = new JwtUtil("01234567890123456789012345678901", 60_000L);

    assertFalse(jwtUtil.isTokenValid("invalid-token"));
  }

  @Test
  void shouldRejectShortSecrets() {
    assertThrows(IllegalArgumentException.class, () -> new JwtUtil("short-secret", 60_000L));
  }
}
