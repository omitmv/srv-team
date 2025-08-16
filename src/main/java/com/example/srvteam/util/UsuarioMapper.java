package com.example.srvteam.util;

import com.example.srvteam.dto.request.UsuarioRequest;
import com.example.srvteam.dto.response.UsuarioResponse;
import com.example.srvteam.model.Usuario;

public class UsuarioMapper {

    /**
     * Converte UsuarioRequest para Usuario
     */
    public static Usuario toEntity(UsuarioRequest request) {
        if (request == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(request.getLogin());
        usuario.setSenha(request.getSenha());
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setCdTpAcesso(request.getCdTpAcesso());
        usuario.setFlAtivo(request.getFlAtivo());
        usuario.setDtExpiracao(request.getDtExpiracao());

        return usuario;
    }

    /**
     * Converte Usuario para UsuarioResponse
     */
    public static UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioResponse(
                usuario.getCdUsuario(),
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDataCadastro(),
                usuario.getFlAtivo(),
                usuario.getDtExpiracao(),
                usuario.getCdTpAcesso()
        );
    }

    /**
     * Atualiza um Usuario existente com dados do UsuarioRequest
     */
    public static void updateEntity(Usuario usuario, UsuarioRequest request) {
        if (usuario == null || request == null) {
            return;
        }

        usuario.setLogin(request.getLogin());
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setCdTpAcesso(request.getCdTpAcesso());
        
        if (request.getFlAtivo() != null) {
            usuario.setFlAtivo(request.getFlAtivo());
        }
        
        usuario.setDtExpiracao(request.getDtExpiracao());

        // Só atualiza a senha se foi fornecida
        if (request.getSenha() != null && !request.getSenha().trim().isEmpty()) {
            usuario.setSenha(request.getSenha());
        }
    }
}
