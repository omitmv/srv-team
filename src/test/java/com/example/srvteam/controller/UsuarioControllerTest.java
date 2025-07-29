package com.example.srvteam.controller;

import com.example.srvteam.dto.request.LoginRequest;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.service.UsuarioService;
import com.example.srvteam.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario sampleUsuario;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        sampleUsuario = new Usuario();
        sampleUsuario.setCdUsuario(1);
        sampleUsuario.setLogin("joao.silva");
        sampleUsuario.setSenha("MjEyMzI1YzcwNjIzYTU0YjJmMDA5NDA5YzJmMDM5MWM="); // 123456 criptografado
        sampleUsuario.setNome("João Silva");
        sampleUsuario.setEmail("joao.silva@example.com");
        sampleUsuario.setDataCadastro(LocalDateTime.now());
        sampleUsuario.setFlAtivo(true);

        // Tokens para teste
        validToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvLnNpbHZhIiwiaWF0IjoxNzUzNTUwMjI2LCJleHAiOjE3NTM2MzY2MjZ9.test";
        invalidToken = "invalid.token.here";

        // Configurar mocks do JwtUtil
        when(jwtUtil.extractLogin(validToken)).thenReturn("joao.silva");
        when(jwtUtil.extractLogin(invalidToken)).thenThrow(new RuntimeException("Invalid token"));
        when(jwtUtil.isTokenValid(validToken)).thenReturn(true);
        when(jwtUtil.isTokenValid(invalidToken)).thenReturn(false);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(validToken);
    }

    // ========== TESTES DE AUTENTICAÇÃO JWT ==========

    @Test
    void testLoginComCredenciaisValidas() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("joao.silva");
        loginRequest.setSenha("123456");

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setCdUsuario(1);
        usuarioAutenticado.setLogin("joao.silva");
        usuarioAutenticado.setNome("João Silva");

        when(usuarioService.autenticarUsuario("joao.silva", "123456")).thenReturn(usuarioAutenticado);

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(validToken))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void testLoginComCredenciaisInvalidas() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("joao.silva");
        loginRequest.setSenha("senha_errada");

        when(usuarioService.autenticarUsuario("joao.silva", "senha_errada"))
                .thenThrow(new IllegalArgumentException("Login ou senha inválidos"));

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ========== TESTES COM AUTENTICAÇÃO MOCK ==========

    @Test
    @WithMockUser(username = "joao.silva")
    void testGetAllUsuariosComAutenticacao() throws Exception {
        when(usuarioService.getAllUsuarios()).thenReturn(Arrays.asList(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("joao.silva"))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].senha").doesNotExist()); // Senha não deve aparecer
    }

    @Test
    @WithMockUser(username = "joao.silva")
    void testGetUsuarioByIdComAutenticacao() throws Exception {
        when(usuarioService.getUsuario(1)).thenReturn(Optional.of(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("joao.silva"))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.senha").doesNotExist()); // Senha não deve aparecer
    }

    @Test
    @WithMockUser(username = "joao.silva")
    void testGetUsuarioByIdNotFound() throws Exception {
        when(usuarioService.getUsuario(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "joao.silva")
    void testGetUsuariosAtivosComAutenticacao() throws Exception {
        when(usuarioService.getUsuariosAtivos()).thenReturn(Arrays.asList(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("joao.silva"))
                .andExpect(jsonPath("$[0].flAtivo").value(true))
                .andExpect(jsonPath("$[0].senha").doesNotExist()); // Senha não deve aparecer
    }

    @Test
    @WithMockUser(username = "joao.silva")
    void testDeleteUsuarioComAutenticacao() throws Exception {
        mockMvc.perform(delete("/v1/usuarios/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== TESTES DE ENDPOINTS PÚBLICOS ==========

    @Test
    void testCriarUsuarioEndpointPublico() throws Exception {
        // Arrange
        Usuario novoUsuario = new Usuario();
        novoUsuario.setLogin("novo.usuario");
        novoUsuario.setSenha("123456"); // Senha antes da criptografia
        novoUsuario.setNome("Novo Usuário");
        novoUsuario.setEmail("novo@example.com");
        novoUsuario.setFlAtivo(true);

        Usuario usuarioCriado = new Usuario();
        usuarioCriado.setCdUsuario(2);
        usuarioCriado.setLogin("novo.usuario");
        usuarioCriado.setNome("Novo Usuário");
        usuarioCriado.setEmail("novo@example.com");
        usuarioCriado.setDataCadastro(LocalDateTime.now());
        usuarioCriado.setFlAtivo(true);

        when(usuarioService.insUsuario(any(Usuario.class))).thenReturn(usuarioCriado);

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoUsuario))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("novo.usuario"))
                .andExpect(jsonPath("$.nome").value("Novo Usuário"))
                .andExpect(jsonPath("$.senha").doesNotExist()); // Senha não deve aparecer
    }

    // ========== TESTES DE VALIDAÇÃO DE DADOS ==========

    @Test
    @WithMockUser(username = "joao.silva")
    void testCriarUsuarioComDadosInvalidos() throws Exception {
        // Arrange - usuário sem login (campo obrigatório)
        Usuario usuarioInvalido = new Usuario();
        usuarioInvalido.setNome("Nome Sem Login");
        usuarioInvalido.setEmail("sem@login.com");

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioInvalido))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSemCorpo() throws Exception {
        mockMvc.perform(post("/v1/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginComDadosVazios() throws Exception {
        LoginRequest loginVazio = new LoginRequest();
        // login e senha ficam null

        mockMvc.perform(post("/v1/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginVazio)))
                .andExpect(status().isBadRequest());
    }
}
