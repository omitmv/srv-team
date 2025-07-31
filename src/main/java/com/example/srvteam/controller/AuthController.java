package com.example.srvteam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.LoginRequest;
import com.example.srvteam.service.UsuarioService;
import com.example.srvteam.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /auth/login - Endpoint alternativo para autenticação
     * Redireciona para o mesmo método do UsuarioController
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Delega para o UsuarioController a lógica de autenticação
        try {
            // Autentica o usuário
            var usuario = usuarioService.autenticarUsuario(loginRequest.getLogin(), loginRequest.getSenha());

            // Gera o token JWT
            String token = jwtUtil.generateToken(usuario.getLogin(), usuario.getCdUsuario(), usuario.getNome());

            // Cria a resposta
            var response = new com.example.srvteam.dto.response.LoginResponse(
                    token,
                    usuario.getCdUsuario(),
                    usuario.getLogin(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    86400000L, // 24 horas em milissegundos
                    usuario.getCdTpAcesso()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
