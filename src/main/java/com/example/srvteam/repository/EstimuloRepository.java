package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Estimulo;

/**
 * Repository para Estimulo
 * Interface para operações de banco de dados da entidade Estimulo
 */
@Repository
public interface EstimuloRepository extends JpaRepository<Estimulo, Integer> {
    
    /**
     * Busca um estímulo pela descrição
     * @param dsEstimulo descrição do estímulo
     * @return Optional contendo o estímulo se encontrado
     */
    Optional<Estimulo> findByDsEstimulo(String dsEstimulo);
    
    /**
     * Lista todos os estímulos ordenados por descrição
     * @return lista de estímulos ordenada
     */
    @Query("SELECT e FROM Estimulo e ORDER BY e.dsEstimulo")
    List<Estimulo> findAllOrderByDsEstimulo();
    
    /**
     * Verifica se existe um estímulo com a descrição informada
     * @param dsEstimulo descrição do estímulo
     * @return true se existe, false caso contrário
     */
    boolean existsByDsEstimulo(String dsEstimulo);
    
    /**
     * Verifica se existe um estímulo com a descrição informada, excluindo um estímulo específico
     * @param dsEstimulo descrição do estímulo
     * @param cdEstimulo código do estímulo a ser excluído da verificação
     * @return true se existe, false caso contrário
     */
    boolean existsByDsEstimuloAndCdEstimuloNot(String dsEstimulo, Integer cdEstimulo);
}
