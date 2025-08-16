package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Menu;

/**
 * Repository para Menu
 * Interface para operações de banco de dados da entidade Menu
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    
    /**
     * Busca um menu pela descrição
     * @param dsMenu descrição do menu
     * @return Optional contendo o menu se encontrado
     */
    Optional<Menu> findByDsMenu(String dsMenu);
    
    /**
     * Lista todos os menus de um sistema específico
     * @param cdSistema código do sistema
     * @return lista de menus do sistema
     */
    @Query("SELECT m FROM Menu m WHERE m.cdSistema = :cdSistema ORDER BY m.dsMenu")
    List<Menu> findByCdSistemaOrderByDsMenu(@Param("cdSistema") Integer cdSistema);
    
    /**
     * Lista todos os menus de um sistema específico (método alternativo)
     * @param cdSistema código do sistema
     * @return lista de menus do sistema
     */
    List<Menu> findByCdSistema(Integer cdSistema);
    
    /**
     * Verifica se existe um menu com a descrição informada
     * @param dsMenu descrição do menu
     * @return true se existe, false caso contrário
     */
    boolean existsByDsMenu(String dsMenu);
    
    /**
     * Verifica se existe um menu com a descrição informada, excluindo um menu específico
     * @param dsMenu descrição do menu
     * @param cdMenu código do menu a ser excluído da verificação
     * @return true se existe, false caso contrário
     */
    boolean existsByDsMenuAndCdMenuNot(String dsMenu, Integer cdMenu);
    
    /**
     * Conta quantos menus existem para um sistema
     * @param cdSistema código do sistema
     * @return quantidade de menus
     */
    long countByCdSistema(Integer cdSistema);
}
