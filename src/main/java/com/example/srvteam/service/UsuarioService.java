package com.example.srvteam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.common.EnumTpAcesso;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.repository.UsuarioRepository;
import com.example.srvteam.util.PasswordUtil;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inserir novo usuário
     * 
     * @param usuario Dados do usuário
     * @return Usuario criado
     */
    public Usuario insUsuario(Usuario usuario) {
        // Verificar se já existe usuário com esse login
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new IllegalArgumentException("Já existe um usuário com esse login: " + usuario.getLogin());
        }

        // Verificar se já existe usuário com esse email
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário com esse email: " + usuario.getEmail());
        }

        // Criptografar a senha
        String senhaCriptografada = PasswordUtil.criptografarSenha(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);

        // Definir valores padrão se não informados
        if (usuario.getFlAtivo() == null) {
            usuario.setFlAtivo(true);
        }
        
        // Definir tipo de acesso padrão se não informado
        if (usuario.getCdTpAcesso() == null) {
            usuario.setCdTpAcesso(EnumTpAcesso.ATLETA.getCodigo());
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Atualizar usuário existente
     * 
     * @param cdUsuario         ID do usuário
     * @param usuarioAtualizado Dados atualizados
     * @return Usuario atualizado
     */
    public Usuario updUsuario(Integer cdUsuario, Usuario usuarioAtualizado) {
        Usuario usuario = usuarioRepository.findById(cdUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + cdUsuario));

        // Verificar se o novo login já está em uso por outro usuário
        if (!usuario.getLogin().equals(usuarioAtualizado.getLogin()) &&
                usuarioRepository.existsByLogin(usuarioAtualizado.getLogin())) {
            throw new IllegalArgumentException("Já existe um usuário com esse login: " + usuarioAtualizado.getLogin());
        }

        // Verificar se o novo email já está em uso por outro usuário
        if (!usuario.getEmail().equals(usuarioAtualizado.getEmail()) &&
                usuarioRepository.existsByEmail(usuarioAtualizado.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário com esse email: " + usuarioAtualizado.getEmail());
        }

        // Atualizar campos
        usuario.setLogin(usuarioAtualizado.getLogin());
        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(usuarioAtualizado.getEmail());
        usuario.setFlAtivo(usuarioAtualizado.getFlAtivo());
        usuario.setDtExpiracao(usuarioAtualizado.getDtExpiracao());
        
        // Atualizar tipo de acesso se informado
        if (usuarioAtualizado.getCdTpAcesso() != null) {
            usuario.setCdTpAcesso(usuarioAtualizado.getCdTpAcesso());
        }

        // Se a senha foi informada, criptografar
        if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isEmpty()) {
            String senhaCriptografada = PasswordUtil.criptografarSenha(usuarioAtualizado.getSenha());
            usuario.setSenha(senhaCriptografada);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Deletar usuário
     * 
     * @param cdUsuario ID do usuário
     */
    public void delUsuario(Integer cdUsuario) {
        Usuario usuario = usuarioRepository.findById(cdUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + cdUsuario));
        usuarioRepository.delete(usuario);
    }

    /**
     * Obter usuário por ID
     * 
     * @param cdUsuario ID do usuário
     * @return Usuario encontrado
     */
    public Optional<Usuario> getUsuario(Integer cdUsuario) {
        return usuarioRepository.findById(cdUsuario);
    }

    /**
     * Listar todos os usuários
     * 
     * @return Lista de usuários
     */
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar usuários ativos
     * 
     * @return Lista de usuários ativos
     */
    public List<Usuario> getUsuariosAtivos() {
        return usuarioRepository.findByFlAtivoTrue();
    }

    /**
     * Buscar usuário por login
     * 
     * @param login Login do usuário
     * @return Usuario encontrado
     */
    public Optional<Usuario> getUsuarioPorLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    /**
     * Buscar usuário por email
     * 
     * @param email Email do usuário
     * @return Usuario encontrado
     */
    public Optional<Usuario> getUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Verificar login e senha
     * 
     * @param login Login do usuário
     * @param senha Senha em texto puro
     * @return Usuario se credenciais válidas
     */
    public Optional<Usuario> verificarCredenciais(String login, String senha) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailOrLoginAndFlAtivoTrue(login);

        if (usuario.isPresent()) {
            boolean senhaValida = PasswordUtil.verificarSenha(senha, usuario.get().getSenha());
            if (senhaValida) {
                // Verificar se não está expirado
                if (usuario.get().getDtExpiracao() == null ||
                        usuario.get().getDtExpiracao().isAfter(LocalDateTime.now())) {
                    return usuario;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Inativar usuário (soft delete)
     * 
     * @param cdUsuario ID do usuário
     */
    public void inativarUsuario(Integer cdUsuario) {
        Usuario usuario = usuarioRepository.findById(cdUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + cdUsuario));
        usuario.setFlAtivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Ativar usuário
     * 
     * @param cdUsuario ID do usuário
     */
    public void ativarUsuario(Integer cdUsuario) {
        Usuario usuario = usuarioRepository.findById(cdUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + cdUsuario));
        usuario.setFlAtivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Autentica um usuário com login e senha
     * 
     * @param login Login do usuário
     * @param senha Senha do usuário
     * @return Usuario autenticado
     */
    public Usuario autenticarUsuario(String login, String senha) {
        // Busca o usuário pelo login
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Login ou senha inválidos");
        }

        Usuario usuario = usuarioOpt.get();

        // Verifica se o usuário está ativo
        if (!usuario.getFlAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        // Verifica se o usuário não expirou
        if (usuario.getDtExpiracao() != null && usuario.getDtExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Usuário expirado");
        }

        // Criptografa a senha informada e compara com a armazenada
        String senhaCriptografada = PasswordUtil.criptografarSenha(senha);
        if (!senhaCriptografada.equals(usuario.getSenha())) {
            throw new IllegalArgumentException("Login ou senha inválidos");
        }

        return usuario;
    }
}
