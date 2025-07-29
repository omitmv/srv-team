package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Tecnica;

/**
 * Repository para Tecnica
 * Interface para operações de banco de dados da entidade Tecnica
 */
@Repository
public interface TecnicaRepository extends JpaRepository<Tecnica, Integer> {
    
    /**
     * Busca uma técnica pela descrição
     * @param dsTecnica descrição da técnica
     * @return Optional contendo a técnica se encontrada
     */
    Optional<Tecnica> findByDsTecnica(String dsTecnica);
    
    /**
     * Lista todas as técnicas ordenadas por descrição
     * @return lista de técnicas ordenada
     */
    @Query("SELECT t FROM Tecnica t ORDER BY t.dsTecnica")
    List<Tecnica> findAllOrderByDsTecnica();
    
    /**
     * Verifica se existe uma técnica com a descrição informada
     * @param dsTecnica descrição da técnica
     * @return true se existe, false caso contrário
     */
    boolean existsByDsTecnica(String dsTecnica);
    
    /**
     * Verifica se existe uma técnica com a descrição informada, excluindo uma técnica específica
     * @param dsTecnica descrição da técnica
     * @param cdTecnica código da técnica a ser excluída da verificação
     * @return true se existe, false caso contrário
     */
    boolean existsByDsTecnicaAndCdTecnicaNot(String dsTecnica, Integer cdTecnica);
}
