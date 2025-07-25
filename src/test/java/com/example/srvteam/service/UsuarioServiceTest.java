package com.example.srvteam.service;

import com.example.srvteam.model.Usuario;
import com.example.srvteam.repository.UsuarioRepository;
import com.example.srvteam.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario sampleUsuario;

    @BeforeEach
    void setUp() {
        sampleUsuario = new Usuario();
        sampleUsuario.setCdUsuario(1);
        sampleUsuario.setLogin("testuser");
        sampleUsuario.setSenha("123456");
        sampleUsuario.setNome("Usuário Teste");
        sampleUsuario.setEmail("teste@example.com");
        sampleUsuario.setFlAtivo(true);
        sampleUsuario.setDataCadastro(LocalDateTime.now());
    }

    @Test
    void testInsUsuario() {
        when(usuarioRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(sampleUsuario);

        Usuario resultado = usuarioService.insUsuario(sampleUsuario);

        assertNotNull(resultado);
        assertEquals("testuser", resultado.getLogin());
        assertEquals("Usuário Teste", resultado.getNome());
        assertTrue(resultado.getFlAtivo());
    }

    @Test
    void testInsUsuarioLoginExistente() {
        when(usuarioRepository.existsByLogin("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.insUsuario(sampleUsuario);
        });
    }

    @Test
    void testGetUsuario() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(sampleUsuario));

        Optional<Usuario> resultado = usuarioService.getUsuario(1);

        assertTrue(resultado.isPresent());
        assertEquals("testuser", resultado.get().getLogin());
    }

    @Test
    void testVerificarCredenciais() {
        // Criptografar a senha do usuário de exemplo
        String senhaCriptografada = PasswordUtil.criptografarSenha("123456");
        sampleUsuario.setSenha(senhaCriptografada);
        
        when(usuarioRepository.findByEmailOrLoginAndFlAtivoTrue("testuser"))
                .thenReturn(Optional.of(sampleUsuario));

        Optional<Usuario> resultado = usuarioService.verificarCredenciais("testuser", "123456");

        assertTrue(resultado.isPresent());
        assertEquals("testuser", resultado.get().getLogin());
    }

    @Test
    void testVerificarCredenciaisInvalidas() {
        String senhaCriptografada = PasswordUtil.criptografarSenha("123456");
        sampleUsuario.setSenha(senhaCriptografada);
        
        when(usuarioRepository.findByEmailOrLoginAndFlAtivoTrue("testuser"))
                .thenReturn(Optional.of(sampleUsuario));

        Optional<Usuario> resultado = usuarioService.verificarCredenciais("testuser", "senhaerrada");

        assertTrue(resultado.isEmpty());
    }
}
