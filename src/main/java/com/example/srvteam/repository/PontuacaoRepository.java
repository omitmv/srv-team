package com.example.srvteam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Pontuacao;

@Repository
public interface PontuacaoRepository extends JpaRepository<Pontuacao, Integer> {
    List<Pontuacao> findByCdCompeticao(Integer cdCompeticao);
}
