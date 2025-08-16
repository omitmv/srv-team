package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.TreinoEstimulo;
import com.example.srvteam.model.TreinoEstimuloId;

/**
 * Repository para TreinoEstimulo
 * Interface para operações de banco de dados da entidade TreinoEstimulo
 */
@Repository
public interface TreinoEstimuloRepository extends JpaRepository<TreinoEstimulo, TreinoEstimuloId> {

    /**
     * Buscar TreinoEstimulo por código do treino e código do estímulo
     * 
     * @param cdTreino Código do treino
     * @param cdEstimulo Código do estímulo
     * @return Optional<TreinoEstimulo>
     */
    @Query("SELECT te FROM TreinoEstimulo te WHERE te.cdTreino = :cdTreino AND te.cdEstimulo = :cdEstimulo")
    Optional<TreinoEstimulo> findByCdTreinoAndCdEstimulo(@Param("cdTreino") Integer cdTreino, 
                                                         @Param("cdEstimulo") Integer cdEstimulo);

    /**
     * Listar todos os estímulos de um treino específico
     * 
     * @param cdTreino Código do treino
     * @return List<TreinoEstimulo>
     */
    @Query("SELECT te FROM TreinoEstimulo te WHERE te.cdTreino = :cdTreino")
    List<TreinoEstimulo> findByCdTreino(@Param("cdTreino") Integer cdTreino);

    /**
     * Listar todos os treinos de um estímulo específico
     * 
     * @param cdEstimulo Código do estímulo
     * @return List<TreinoEstimulo>
     */
    @Query("SELECT te FROM TreinoEstimulo te WHERE te.cdEstimulo = :cdEstimulo")
    List<TreinoEstimulo> findByCdEstimulo(@Param("cdEstimulo") Integer cdEstimulo);

    /**
     * Verificar se existe TreinoEstimulo com o treino e estímulo informados
     * 
     * @param cdTreino Código do treino
     * @param cdEstimulo Código do estímulo
     * @return boolean
     */
    @Query("SELECT COUNT(te) > 0 FROM TreinoEstimulo te WHERE te.cdTreino = :cdTreino AND te.cdEstimulo = :cdEstimulo")
    boolean existsByCdTreinoAndCdEstimulo(@Param("cdTreino") Integer cdTreino, @Param("cdEstimulo") Integer cdEstimulo);

    /**
     * Deletar todos os TreinoEstimulo de um treino específico
     * 
     * @param cdTreino Código do treino
     */
    @Query("DELETE FROM TreinoEstimulo te WHERE te.cdTreino = :cdTreino")
    void deleteByCdTreino(@Param("cdTreino") Integer cdTreino);

    /**
     * Deletar todos os TreinoEstimulo de um estímulo específico
     * 
     * @param cdEstimulo Código do estímulo
     */
    @Query("DELETE FROM TreinoEstimulo te WHERE te.cdEstimulo = :cdEstimulo")
    void deleteByCdEstimulo(@Param("cdEstimulo") Integer cdEstimulo);
}
