package com.example.srvteam.repository;

import com.example.srvteam.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    // Buscar por nome (case insensitive)
    Optional<Team> findByNameIgnoreCase(String name);
    
    // Buscar times que contenham uma string no nome
    List<Team> findByNameContainingIgnoreCase(String name);
    
    // Buscar por team lead
    List<Team> findByTeamLead(String teamLead);
    
    // Buscar por email
    Optional<Team> findByEmail(String email);
    
    // Query customizada para buscar por palavras-chave na descrição
    @Query("SELECT t FROM Team t WHERE t.description LIKE %:keyword%")
    List<Team> findByDescriptionContaining(@Param("keyword") String keyword);
    
    // Verificar se existe um time com o nome especificado
    boolean existsByNameIgnoreCase(String name);
}
