package com.example.srvteam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.dto.request.PontuacaoHistRequest;
import com.example.srvteam.dto.response.PontuacaoHistResponse;
import com.example.srvteam.mapper.PontuacaoHistMapper;
import com.example.srvteam.model.PontuacaoHist;
import com.example.srvteam.repository.PontuacaoHistRepository;

import jakarta.transaction.Transactional;

@Service
public class PontuacaoHistService {
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
}
