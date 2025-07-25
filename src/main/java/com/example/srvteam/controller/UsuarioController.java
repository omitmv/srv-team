package com.example.srvteam.controller;

import com.example.srvteam.model.Usuario;
import com.example.srvteam.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * POST /v1/usuarios - Inserir novo usuário
     */
    @PostMapping
    public ResponseEntity<?> insUsuario(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario usuarioCriado = usuarioService.insUsuario(usuario);
            // Remover senha da resposta por segurança
            usuarioCriado.setSenha(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * PUT /v1/usuarios/{cdUsuario} - Atualizar usuário existente
     */
    @PutMapping("/{cdUsuario}")
    public ResponseEntity<?> updUsuario(@PathVariable Integer cdUsuario, @Valid @RequestBody Usuario usuario) {
        try {
            Usuario usuarioAtualizado = usuarioService.updUsuario(cdUsuario, usuario);
            // Remover senha da resposta por segurança
            usuarioAtualizado.setSenha(null);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * DELETE /v1/usuarios/{cdUsuario} - Deletar usuário
     */
    @DeleteMapping("/{cdUsuario}")
    public ResponseEntity<?> delUsuario(@PathVariable Integer cdUsuario) {
        try {
            usuarioService.delUsuario(cdUsuario);
            return ResponseEntity.ok().body("Usuário deletado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /v1/usuarios/{cdUsuario} - Obter usuário por ID
     */
    @GetMapping("/{cdUsuario}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable Integer cdUsuario) {
        Optional<Usuario> usuario = usuarioService.getUsuario(cdUsuario);
        if (usuario.isPresent()) {
            // Remover senha da resposta por segurança
            usuario.get().setSenha(null);
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /v1/usuarios - Listar todos os usuários
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        // Remover senhas da resposta por segurança
        usuarios.forEach(u -> u.setSenha(null));
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * GET /v1/usuarios/ativos - Listar usuários ativos
     */
    @GetMapping("/ativos")
    public ResponseEntity<List<Usuario>> getUsuariosAtivos() {
        List<Usuario> usuarios = usuarioService.getUsuariosAtivos();
        // Remover senhas da resposta por segurança
        usuarios.forEach(u -> u.setSenha(null));
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * GET /v1/usuarios/login/{login} - Buscar usuário por login
     */
    @GetMapping("/login/{login}")
    public ResponseEntity<Usuario> getUsuarioPorLogin(@PathVariable String login) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorLogin(login);
        if (usuario.isPresent()) {
            // Remover senha da resposta por segurança
            usuario.get().setSenha(null);
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /v1/usuarios/email/{email} - Buscar usuário por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> getUsuarioPorEmail(@PathVariable String email) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorEmail(email);
        if (usuario.isPresent()) {
            // Remover senha da resposta por segurança
            usuario.get().setSenha(null);
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * POST /v1/usuarios/login - Verificar credenciais de login
     */
    @PostMapping("/login")
    public ResponseEntity<?> verificarCredenciais(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuario = usuarioService.verificarCredenciais(
            loginRequest.getLogin(), 
            loginRequest.getSenha()
        );
        
        if (usuario.isPresent()) {
            // Remover senha da resposta por segurança
            usuario.get().setSenha(null);
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }
    
    /**
     * PATCH /v1/usuarios/{cdUsuario}/inativar - Inativar usuário
     */
    @PatchMapping("/{cdUsuario}/inativar")
    public ResponseEntity<?> inativarUsuario(@PathVariable Integer cdUsuario) {
        try {
            usuarioService.inativarUsuario(cdUsuario);
            return ResponseEntity.ok().body("Usuário inativado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * PATCH /v1/usuarios/{cdUsuario}/ativar - Ativar usuário
     */
    @PatchMapping("/{cdUsuario}/ativar")
    public ResponseEntity<?> ativarUsuario(@PathVariable Integer cdUsuario) {
        try {
            usuarioService.ativarUsuario(cdUsuario);
            return ResponseEntity.ok().body("Usuário ativado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Classe auxiliar para request de login
    public static class LoginRequest {
        private String login;
        private String senha;
        
        public String getLogin() {
            return login;
        }
        
        public void setLogin(String login) {
            this.login = login;
        }
        
        public String getSenha() {
            return senha;
        }
        
        public void setSenha(String senha) {
            this.senha = senha;
        }
    }
}
