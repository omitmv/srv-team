package com.example.srvteam.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.LoginRequest;
import com.example.srvteam.dto.LoginResponse;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.service.UsuarioService;
import com.example.srvteam.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/usuario")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

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
     * POST /v1/usuarios/login - Autenticar usuário e gerar JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autentica o usuário
            Usuario usuario = usuarioService.autenticarUsuario(loginRequest.getLogin(), loginRequest.getSenha());

            // Gera o token JWT
            String token = jwtUtil.generateToken(usuario.getLogin(), usuario.getCdUsuario(), usuario.getNome());

            // Cria a resposta
            LoginResponse response = new LoginResponse(
                    token,
                    usuario.getCdUsuario(),
                    usuario.getLogin(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    86400000L // 24 horas em milissegundos
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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
}
