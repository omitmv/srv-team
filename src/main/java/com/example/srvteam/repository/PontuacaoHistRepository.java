package com.example.srvteam.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.PontuacaoHist;

@Repository
public interface PontuacaoHistRepository extends JpaRepository<PontuacaoHist, Integer> {
	List<PontuacaoHist> findByCdCompeticao(Integer cdCompeticao);
	List<PontuacaoHist> findByCdCompetidorAndCdCompeticao(Integer cdCompetidor, Integer cdCompeticao);
	List<PontuacaoHist> findByCdCompetidorAndDtCadastroBetween(Integer cdCompetidor, LocalDateTime inicio, LocalDateTime fim);
	@Query(value = "select c.nome, d.nm_competicao as nmCompeticao, SUM(b.pontuacao) as totalPontuacao from tb_pontuacao_hist a inner join tb_pontuacao b ON( b.cd_pontuacao = a.cd_pontuacao ) inner join tb_usuario c ON( c.cd_usuario = a.cd_competidor ) inner join tb_competicao d ON( d.cd_competicao = a.cd_competicao ) where a.cd_competidor = :cdCompetidor and a.cd_competicao = :cdCompeticao GROUP BY c.nome, d.nm_competicao", nativeQuery = true)
	java.util.List<Object[]> findPontuacaoHistResumo(@Param("cdCompetidor") Integer cdCompetidor, @Param("cdCompeticao") Integer cdCompeticao);
}
