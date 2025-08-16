package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Treino;

/**
 * Repository para Treino
 * Interface para operações de banco de dados da entidade Treino
 */
@Repository
public interface TreinoRepository extends JpaRepository<Treino, Integer> {
    
    /**
     * Busca um treino pela descrição
     * @param dsTreino descrição do treino
     * @return Optional contendo o treino se encontrado
     */
    Optional<Treino> findByDsTreino(String dsTreino);
    
    /**
     * Lista todos os treinos de um atleta específico ordenados por data de cadastro
     * @param cdAtleta código do atleta
     * @return lista de treinos do atleta
     */
    @Query("SELECT t FROM Treino t WHERE t.cdAtleta = :cdAtleta ORDER BY t.dtCadastro DESC")
    List<Treino> findByCdAtletaOrderByDtCadastroDesc(@Param("cdAtleta") Integer cdAtleta);
    
    /**
     * Lista todos os treinos de um profissional específico ordenados por data de cadastro
     * @param cdProfissional código do profissional
     * @return lista de treinos do profissional
     */
    @Query("SELECT t FROM Treino t WHERE t.cdProfissional = :cdProfissional ORDER BY t.dtCadastro DESC")
    List<Treino> findByCdProfissionalOrderByDtCadastroDesc(@Param("cdProfissional") Integer cdProfissional);
    
    /**
     * Lista todos os treinos de um atleta específico e de um profissional específico
     * @param cdAtleta código do atleta
     * @param cdProfissional código do profissional
     * @return lista de treinos filtrados
     */
    @Query("SELECT t FROM Treino t WHERE t.cdAtleta = :cdAtleta AND t.cdProfissional = :cdProfissional ORDER BY t.dtCadastro DESC")
    List<Treino> findByCdAtletaAndCdProfissionalOrderByDtCadastroDesc(@Param("cdAtleta") Integer cdAtleta, 
                                                                      @Param("cdProfissional") Integer cdProfissional);
    
    /**
     * Verifica se existe um treino com a descrição informada
     * @param dsTreino descrição do treino
     * @return true se existe, false caso contrário
     */
    boolean existsByDsTreino(String dsTreino);
    
    /**
     * Lista todos os treinos ordenados por data de cadastro (mais recentes primeiro)
     * @return lista de treinos ordenada
     */
    @Query("SELECT t FROM Treino t ORDER BY t.dtCadastro DESC")
    List<Treino> findAllOrderByDtCadastroDesc();
    
    /**
     * Verifica se existe treino com a descrição informada, excluindo um treino específico
     * @param dsTreino descrição do treino
     * @param cdTreino código do treino a ser excluído da verificação
     * @return true se existe outro treino com a mesma descrição
     */
    @Query("SELECT COUNT(t) > 0 FROM Treino t WHERE t.dsTreino = :dsTreino AND t.cdTreino != :cdTreino")
    boolean existsByDsTreinoAndCdTreinoNot(@Param("dsTreino") String dsTreino, @Param("cdTreino") Integer cdTreino);
}
