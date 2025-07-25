package com.example.srvteam.repository;

import com.example.srvteam.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Buscar por login
    Optional<Usuario> findByLogin(String login);
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Verificar se existe usuário com login
    boolean existsByLogin(String login);
    
    // Verificar se existe usuário com email
    boolean existsByEmail(String email);
    
    // Buscar usuários ativos
    List<Usuario> findByFlAtivoTrue();
    
    // Buscar usuários inativos
    List<Usuario> findByFlAtivoFalse();
    
    // Buscar por nome (case insensitive)
    List<Usuario> findByNomeContainingIgnoreCase(String nome);
    
    // Query customizada para buscar usuários com expiração próxima
    @Query("SELECT u FROM Usuario u WHERE u.dtExpiracao IS NOT NULL AND u.dtExpiracao <= CURRENT_TIMESTAMP AND u.flAtivo = true")
    List<Usuario> findUsuariosExpirados();
    
    // Query customizada para buscar usuários ativos por email ou login
    @Query("SELECT u FROM Usuario u WHERE (u.email = :emailOuLogin OR u.login = :emailOuLogin) AND u.flAtivo = true")
    Optional<Usuario> findByEmailOrLoginAndFlAtivoTrue(@Param("emailOuLogin") String emailOuLogin);
}
