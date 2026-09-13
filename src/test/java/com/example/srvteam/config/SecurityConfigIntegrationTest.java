package com.example.srvteam.config;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.srvteam.filter.JwtAuthenticationFilter;
import com.example.srvteam.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SecurityConfigIntegrationTest.TestConfig.class)
class SecurityConfigIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JwtUtil jwtUtil;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void shouldRejectProtectedEndpointWithoutToken() throws Exception {
    mockMvc.perform(get("/secure"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowPublicLoginEndpointWithoutToken() throws Exception {
    mockMvc.perform(post("/v1/usuario/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAuthenticateProtectedEndpointWithValidBearerToken() throws Exception {
    String token = jwtUtil.generateToken("tester", 7, "Test User");

    mockMvc.perform(get("/secure")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(header().string("X-User-Login", "tester"))
        .andExpect(header().string("X-User-Id", "7"))
        .andExpect(header().string("X-User-Name", "Test User"));
  }

  @Configuration
  @EnableWebMvc
  @EnableWebSecurity
  @Import(SecurityConfig.class)
  static class TestConfig {

    @Bean
    JwtUtil jwtUtil() {
      return new JwtUtil("01234567890123456789012345678901", 86_400_000L);
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
      return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    TestController testController() {
      return new TestController();
    }
  }

  @RestController
  static class TestController {
    @PostMapping("/v1/usuario/login")
    void login() {
    }

    @GetMapping("/secure")
    String secure() {
      return "ok";
    }
  }
}
