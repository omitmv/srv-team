
package com.example.srvteam.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.PontuacaoHist;

@Repository
public interface PontuacaoHistRepository extends JpaRepository<PontuacaoHist, Integer> {
	List<PontuacaoHist> findByCdCompeticao(Integer cdCompeticao);
	List<PontuacaoHist> findByCdCompetidorAndCdCompeticao(Integer cdCompetidor, Integer cdCompeticao);
	List<PontuacaoHist> findByCdCompetidorAndDtCadastroBetween(Integer cdCompetidor, LocalDateTime inicio, LocalDateTime fim);
}
