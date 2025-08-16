package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Exercicio;

/**
 * Repository para Exercicio
 * Interface para operações de banco de dados da entidade Exercicio
 */
@Repository
public interface ExercicioRepository extends JpaRepository<Exercicio, Integer> {
    
    /**
     * Busca um exercício pela descrição
     * @param dsExercicio descrição do exercício
     * @return Optional contendo o exercício se encontrado
     */
    Optional<Exercicio> findByDsExercicio(String dsExercicio);
    
    /**
     * Lista todos os exercícios de um grupo muscular específico ordenados por descrição
     * @param cdGrupoMuscular código do grupo muscular
     * @return lista de exercícios do grupo muscular
     */
    @Query("SELECT e FROM Exercicio e WHERE e.cdGrupoMuscular = :cdGrupoMuscular ORDER BY e.dsExercicio")
    List<Exercicio> findByCdGrupoMuscularOrderByDsExercicio(@Param("cdGrupoMuscular") Integer cdGrupoMuscular);
    
    /**
     * Lista todos os exercícios de um grupo muscular específico (método alternativo)
     * @param cdGrupoMuscular código do grupo muscular
     * @return lista de exercícios do grupo muscular
     */
    List<Exercicio> findByCdGrupoMuscular(Integer cdGrupoMuscular);
    
    /**
     * Lista exercícios de um grupo muscular específico filtrando pelo nome usando LIKE
     * @param cdGrupoMuscular código do grupo muscular
     * @param dsExercicio termo para busca no nome do exercício (será usado com LIKE)
     * @return lista de exercícios filtrados
     */
    @Query("SELECT e FROM Exercicio e WHERE e.cdGrupoMuscular = :cdGrupoMuscular AND UPPER(e.dsExercicio) LIKE UPPER(CONCAT('%', :dsExercicio, '%')) ORDER BY e.dsExercicio")
    List<Exercicio> findByCdGrupoMuscularAndDsExercicioContainingIgnoreCase(@Param("cdGrupoMuscular") Integer cdGrupoMuscular, @Param("dsExercicio") String dsExercicio);
    
    /**
     * Lista todos os exercícios ordenados por descrição
     * @return lista de exercícios ordenada
     */
    @Query("SELECT e FROM Exercicio e ORDER BY e.dsExercicio")
    List<Exercicio> findAllOrderByDsExercicio();
    
    /**
     * Verifica se existe um exercício com a descrição informada
     * @param dsExercicio descrição do exercício
     * @return true se existe, false caso contrário
     */
    boolean existsByDsExercicio(String dsExercicio);
    
    /**
     * Verifica se existe um exercício com a descrição informada, excluindo um exercício específico
     * @param dsExercicio descrição do exercício
     * @param cdExercicio código do exercício a ser excluído da verificação
     * @return true se existe, false caso contrário
     */
    boolean existsByDsExercicioAndCdExercicioNot(String dsExercicio, Integer cdExercicio);
    
    /**
     * Conta quantos exercícios existem para um grupo muscular
     * @param cdGrupoMuscular código do grupo muscular
     * @return quantidade de exercícios
     */
    long countByCdGrupoMuscular(Integer cdGrupoMuscular);
}
