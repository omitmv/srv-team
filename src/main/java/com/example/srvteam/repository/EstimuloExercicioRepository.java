package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.EstimuloExercicio;
import com.example.srvteam.model.EstimuloExercicioId;

/**
 * Repository para EstimuloExercicio
 * Interface para operações de banco de dados da entidade EstimuloExercicio
 */
@Repository
public interface EstimuloExercicioRepository extends JpaRepository<EstimuloExercicio, EstimuloExercicioId> {

    /**
     * Buscar EstimuloExercicio por código do estímulo e código do exercício
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @return Optional<EstimuloExercicio>
     */
    @Query("SELECT ee FROM EstimuloExercicio ee WHERE ee.cdEstimulo = :cdEstimulo AND ee.cdExercicio = :cdExercicio")
    Optional<EstimuloExercicio> findByCdEstimuloAndCdExercicio(@Param("cdEstimulo") Integer cdEstimulo, 
                                                               @Param("cdExercicio") Integer cdExercicio);

    /**
     * Listar todos os exercícios de um estímulo específico
     * 
     * @param cdEstimulo Código do estímulo
     * @return List<EstimuloExercicio>
     */
    @Query("SELECT ee FROM EstimuloExercicio ee " +
           "LEFT JOIN FETCH ee.exercicio e " +
           "LEFT JOIN FETCH ee.tecnica t " +
           "WHERE ee.cdEstimulo = :cdEstimulo " +
           "ORDER BY e.dsExercicio")
    List<EstimuloExercicio> findByCdEstimuloOrderByExercicioDesc(@Param("cdEstimulo") Integer cdEstimulo);

    /**
     * Verificar se existe EstimuloExercicio por código do estímulo e código do exercício
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @return boolean
     */
    @Query("SELECT COUNT(ee) > 0 FROM EstimuloExercicio ee WHERE ee.cdEstimulo = :cdEstimulo AND ee.cdExercicio = :cdExercicio")
    boolean existsByCdEstimuloAndCdExercicio(@Param("cdEstimulo") Integer cdEstimulo, 
                                             @Param("cdExercicio") Integer cdExercicio);

    /**
     * Contar quantos exercícios existem em um estímulo
     * 
     * @param cdEstimulo Código do estímulo
     * @return Long
     */
    @Query("SELECT COUNT(ee) FROM EstimuloExercicio ee WHERE ee.cdEstimulo = :cdEstimulo")
    Long countByCdEstimulo(@Param("cdEstimulo") Integer cdEstimulo);

    /**
     * Deletar EstimuloExercicio por código do estímulo e código do exercício
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     */
    @Query("DELETE FROM EstimuloExercicio ee WHERE ee.cdEstimulo = :cdEstimulo AND ee.cdExercicio = :cdExercicio")
    void deleteByCdEstimuloAndCdExercicio(@Param("cdEstimulo") Integer cdEstimulo, 
                                          @Param("cdExercicio") Integer cdExercicio);
}
