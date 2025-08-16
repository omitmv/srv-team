package com.example.srvteam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Competicao;

@Repository
public interface CompeticaoRepository extends JpaRepository<Competicao, Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM tb_competicao c WHERE UPPER(c.nm_competicao) LIKE CONCAT('%', UPPER(:nmCompeticao), '%')", nativeQuery = true)
    List<Competicao> buscarPorNomeLike(@org.springframework.data.repository.query.Param("nmCompeticao") String nmCompeticao);
}
