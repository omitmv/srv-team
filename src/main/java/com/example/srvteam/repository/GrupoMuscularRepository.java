package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.GrupoMuscular;

/**
 * Repository para GrupoMuscular
 * Interface para operações de banco de dados da entidade GrupoMuscular
 */
@Repository
public interface GrupoMuscularRepository extends JpaRepository<GrupoMuscular, Integer> {
    
    /**
     * Busca um grupo muscular pela descrição
     * @param dsGrupoMuscular descrição do grupo muscular
     * @return Optional contendo o grupo muscular se encontrado
     */
    Optional<GrupoMuscular> findByDsGrupoMuscular(String dsGrupoMuscular);
    
    /**
     * Lista todos os grupos musculares ordenados por descrição
     * @return lista de grupos musculares ordenada
     */
    @Query("SELECT g FROM GrupoMuscular g ORDER BY g.dsGrupoMuscular")
    List<GrupoMuscular> findAllOrderByDsGrupoMuscular();
    
    /**
     * Verifica se existe um grupo muscular com a descrição informada
     * @param dsGrupoMuscular descrição do grupo muscular
     * @return true se existe, false caso contrário
     */
    boolean existsByDsGrupoMuscular(String dsGrupoMuscular);
    
    /**
     * Verifica se existe um grupo muscular com a descrição informada, excluindo um grupo específico
     * @param dsGrupoMuscular descrição do grupo muscular
     * @param cdGrupoMuscular código do grupo muscular a ser excluído da verificação
     * @return true se existe, false caso contrário
     */
    boolean existsByDsGrupoMuscularAndCdGrupoMuscularNot(String dsGrupoMuscular, Integer cdGrupoMuscular);
}
