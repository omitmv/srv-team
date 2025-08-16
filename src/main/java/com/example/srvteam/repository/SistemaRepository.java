package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Sistema;

/**
 * Repository para Sistema
 * Interface para operações de banco de dados da entidade Sistema
 */
@Repository
public interface SistemaRepository extends JpaRepository<Sistema, Integer> {
    
    /**
     * Buscar sistema por descrição
     * @param dsSistema descrição do sistema
     * @return Optional<Sistema>
     */
    Optional<Sistema> findByDsSistema(String dsSistema);
    
    /**
     * Verificar se existe sistema com a descrição informada
     * @param dsSistema descrição do sistema
     * @return boolean
     */
    boolean existsByDsSistema(String dsSistema);
    
    /**
     * Buscar todos os sistemas ativos
     * @return List<Sistema>
     */
    List<Sistema> findByFlAtivoTrue();
    
    /**
     * Buscar todos os sistemas inativos
     * @return List<Sistema>
     */
    List<Sistema> findByFlAtivoFalse();
    
    /**
     * Buscar sistema ativo por código
     * @param cdSistema código do sistema
     * @return Optional<Sistema>
     */
    @Query("SELECT s FROM Sistema s WHERE s.cdSistema = :cdSistema AND s.flAtivo = true")
    Optional<Sistema> findActiveByCdSistema(@Param("cdSistema") Integer cdSistema);
    
    /**
     * Verificar se existe sistema ativo com descrição diferente do código atual
     * Usado para validação de unicidade na atualização
     * @param dsSistema descrição do sistema
     * @param cdSistema código do sistema atual
     * @return boolean
     */
    @Query("SELECT COUNT(s) > 0 FROM Sistema s WHERE s.dsSistema = :dsSistema AND s.cdSistema != :cdSistema")
    boolean existsByDsSistemaAndCdSistemaNot(@Param("dsSistema") String dsSistema, @Param("cdSistema") Integer cdSistema);
}
