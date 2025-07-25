package com.example.srvteam.controller;

import com.example.srvteam.model.Usuario;
import com.example.srvteam.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario sampleUsuario;

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
    }

    @Test
    void testGetAllUsuarios() throws Exception {
        when(usuarioService.getAllUsuarios()).thenReturn(Arrays.asList(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("joao.silva"))
                .andExpect(jsonPath("$[0].nome").value("João Silva"));
    }

    @Test
    void testGetUsuarioById() throws Exception {
        when(usuarioService.getUsuario(1)).thenReturn(Optional.of(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("joao.silva"));
    }

    @Test
    void testGetUsuarioByIdNotFound() throws Exception {
        when(usuarioService.getUsuario(any(Integer.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUsuario() throws Exception {
        when(usuarioService.insUsuario(any(Usuario.class))).thenReturn(sampleUsuario);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setLogin("joao.silva");
        novoUsuario.setSenha("123456"); // Senha antes da criptografia
        novoUsuario.setNome("João Silva");
        novoUsuario.setEmail("joao.silva@example.com");

        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("joao.silva"));
    }

    @Test
    void testGetUsuariosAtivos() throws Exception {
        when(usuarioService.getUsuariosAtivos()).thenReturn(Arrays.asList(sampleUsuario));

        mockMvc.perform(get("/v1/usuarios/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("joao.silva"))
                .andExpect(jsonPath("$[0].flAtivo").value(true));
    }

    @Test
    void testDeleteUsuario() throws Exception {
        mockMvc.perform(delete("/v1/usuarios/1"))
                .andExpect(status().isOk());
    }
}
