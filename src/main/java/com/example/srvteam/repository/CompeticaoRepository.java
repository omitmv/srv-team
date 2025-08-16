package com.example.srvteam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Competicao;

@Repository
public interface CompeticaoRepository extends JpaRepository<Competicao, Integer> {
    List<Competicao> findByNmCompeticaoContainingIgnoreCase(String nmCompeticao);
}
