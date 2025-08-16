package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.TimeProfissional;
import com.example.srvteam.model.TimeProfissionalId;

@Repository
public interface TimeProfissionalRepository extends JpaRepository<TimeProfissional, TimeProfissionalId> {

    // Buscar por time e profissional
    Optional<TimeProfissional> findByCdTimeAndCdProfissional(Integer cdTime, Integer cdProfissional);

    // Verificar se existe associação
    boolean existsByCdTimeAndCdProfissional(Integer cdTime, Integer cdProfissional);

    // Listar profissionais de um time
    List<TimeProfissional> findByCdTime(Integer cdTime);

    // Listar times de um profissional
    List<TimeProfissional> findByCdProfissional(Integer cdProfissional);

    // Query customizada para buscar profissionais de um time com detalhes
    @Query("SELECT tp FROM TimeProfissional tp JOIN FETCH tp.profissional WHERE tp.cdTime = :cdTime")
    List<TimeProfissional> findByTimeWithProfissionalDetails(@Param("cdTime") Integer cdTime);

    // Query customizada para buscar times de um profissional com detalhes
    @Query("SELECT tp FROM TimeProfissional tp JOIN FETCH tp.time WHERE tp.cdProfissional = :cdProfissional")
    List<TimeProfissional> findByProfissionalWithTimeDetails(@Param("cdProfissional") Integer cdProfissional);

    // Query customizada para buscar com todos os detalhes
    @Query("SELECT tp FROM TimeProfissional tp " +
           "JOIN FETCH tp.time " +
           "JOIN FETCH tp.profissional " +
           "WHERE tp.cdTime = :cdTime AND tp.cdProfissional = :cdProfissional")
    Optional<TimeProfissional> findByIdWithFullDetails(@Param("cdTime") Integer cdTime, 
                                                       @Param("cdProfissional") Integer cdProfissional);

    // Contar profissionais em um time
    long countByCdTime(Integer cdTime);

    // Contar times de um profissional
    long countByCdProfissional(Integer cdProfissional);

    // Deletar todos os profissionais de um time
    void deleteByCdTime(Integer cdTime);

    // Deletar todos os times de um profissional
    void deleteByCdProfissional(Integer cdProfissional);
}
