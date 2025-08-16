package com.example.srvteam.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.dto.request.PontuacaoHistPorPosicaoRequest;
import com.example.srvteam.dto.request.PontuacaoHistRequest;
import com.example.srvteam.dto.response.PontuacaoHistResponse;
import com.example.srvteam.dto.response.PontuacaoHistResumoResponse;
import com.example.srvteam.mapper.PontuacaoHistMapper;
import com.example.srvteam.model.Pontuacao;
import com.example.srvteam.model.PontuacaoHist;
import com.example.srvteam.repository.CompetidoresRepository;
import com.example.srvteam.repository.PontuacaoHistRepository;
import com.example.srvteam.repository.PontuacaoRepository;

import jakarta.transaction.Transactional;

@Service
public class PontuacaoHistService {
    private static final Logger logger = LoggerFactory.getLogger(PontuacaoHistService.class);
    @Autowired
    private CompetidoresRepository competidoresRepository;

    @Autowired
    private PontuacaoRepository pontuacaoRepository;

    @Autowired
    private PontuacaoHistRepository pontuacaoHistRepository;

    @Transactional
    public PontuacaoHistResponse insPontuacaoHist(PontuacaoHistRequest request) {
        PontuacaoHist entity = PontuacaoHistMapper.toEntity(request);
        PontuacaoHist saved = pontuacaoHistRepository.save(entity);
        return PontuacaoHistMapper.toResponse(saved);
    }

    @Transactional
    public PontuacaoHistResponse updPontuacaoHist(Integer cdPontuacaoHist, PontuacaoHistRequest request) {
        Optional<PontuacaoHist> opt = pontuacaoHistRepository.findById(cdPontuacaoHist);
        if (opt.isEmpty()) throw new RuntimeException("Histórico não encontrado");
        PontuacaoHist entity = opt.get();
        PontuacaoHistMapper.updateEntity(entity, request);
        PontuacaoHist updated = pontuacaoHistRepository.save(entity);
        return PontuacaoHistMapper.toResponse(updated);
    }

    @Transactional
    public void delPontuacaoHist(Integer cdPontuacaoHist) {
        if (!pontuacaoHistRepository.existsById(cdPontuacaoHist))
            throw new RuntimeException("Histórico não encontrado");
        pontuacaoHistRepository.deleteById(cdPontuacaoHist);
    }

    public List<PontuacaoHistResponse> listPontuacaoHistByPeriodo(Integer cdCompetidor, LocalDateTime inicio, LocalDateTime fim) {
        List<PontuacaoHist> list = pontuacaoHistRepository.findByCdCompetidorAndDtCadastroBetween(cdCompetidor, inicio, fim);
        return list.stream().map(PontuacaoHistMapper::toResponse).collect(Collectors.toList());
    }

    public List<PontuacaoHistResponse> listPontuacaoHistByCdCompetidorInCdCompeticao(Integer cdCompetidor, Integer cdCompeticao) {
        List<PontuacaoHist> list = pontuacaoHistRepository.findByCdCompetidorAndCdCompeticao(cdCompetidor, cdCompeticao);
        return list.stream().map(PontuacaoHistMapper::toResponse).collect(Collectors.toList());
    }

    public List<PontuacaoHistResponse> listPontuacaoHistByCdCompeticao(Integer cdCompeticao) {
        List<PontuacaoHist> list = pontuacaoHistRepository.findByCdCompeticao(cdCompeticao);
        return list.stream().map(PontuacaoHistMapper::toResponse).collect(Collectors.toList());
    }

    /**
     * Insere histórico de pontuação por posição, validando regras de negócio
     */
    @Transactional
    public PontuacaoHistResponse insPontuacaoHistPorPosicao(PontuacaoHistPorPosicaoRequest req) {
        // 1. Verificar se o competidor está na competição
        boolean competidorNaCompeticao = competidoresRepository.findByCdCompeticao(req.getCdCompeticao())
            .stream()
            .anyMatch(c -> c.getCdCompetidor().equals(req.getCdCompetidor()));
        if (!competidorNaCompeticao) {
            throw new IllegalArgumentException("Competidor não está inscrito nesta competição.");
        }

        // 2. Buscar cdPontuacao pela competição e posição
        Pontuacao pontuacao = pontuacaoRepository.findByCdCompeticao(req.getCdCompeticao())
            .stream()
            .filter(p -> p.getPosicao().equals(req.getPosicao()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Não existe pontuação para esta posição na competição."));

        // 3. Montar e salvar PontuacaoHist
        PontuacaoHistRequest histRequest = new PontuacaoHistRequest();
        histRequest.setCdCompetidor(req.getCdCompetidor());
        histRequest.setCdCompeticao(req.getCdCompeticao());
        histRequest.setCdPontuacao(pontuacao.getCdPontuacao());
        // dtCadastro será preenchido automaticamente se necessário
        PontuacaoHist entity = PontuacaoHistMapper.toEntity(histRequest);
        PontuacaoHist saved = pontuacaoHistRepository.save(entity);
        return PontuacaoHistMapper.toResponse(saved);
  }

    public PontuacaoHistResumoResponse getPontuacaoHistResumo(Integer cdCompetidor, Integer cdCompeticao) {
        java.util.List<Object[]> results = pontuacaoHistRepository.findPontuacaoHistResumo(cdCompetidor, cdCompeticao);
        if (results == null || results.isEmpty()) {
            return null;
        }
        Object[] result = results.get(0);
        String nome = result[0] != null ? result[0].toString() : "";
        String nmCompeticao = result[1] != null ? result[1].toString() : "";
        BigDecimal totalPontuacao = result[2] != null ? new BigDecimal(result[2].toString()) : BigDecimal.ZERO;
        return new PontuacaoHistResumoResponse(nome, nmCompeticao, totalPontuacao);
    }
}
