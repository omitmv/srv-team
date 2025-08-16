package com.example.srvteam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Time;

@Repository
public interface TimeRepository extends JpaRepository<Time, Integer> {

    // Buscar times por profissional responsável
    List<Time> findByCdProfissionalResponsavel(Integer cdProfissionalResponsavel);

    // Buscar time por nome (verificar duplicatas)
    Optional<Time> findByDsNome(String dsNome);

    // Verificar se existe time com nome
    boolean existsByDsNome(String dsNome);

    // Buscar times por nome (case insensitive)
    List<Time> findByDsNomeContainingIgnoreCase(String dsNome);

    // Query customizada para buscar times com informações do profissional
    @Query("SELECT t FROM Time t JOIN FETCH t.profissionalResponsavel WHERE t.cdProfissionalResponsavel = :cdProfissional")
    List<Time> findByProfissionalWithDetails(@Param("cdProfissional") Integer cdProfissional);

    // Query customizada para buscar time por ID com informações do profissional
    @Query("SELECT t FROM Time t JOIN FETCH t.profissionalResponsavel WHERE t.cdTime = :cdTime")
    Optional<Time> findByIdWithProfissional(@Param("cdTime") Integer cdTime);
}
