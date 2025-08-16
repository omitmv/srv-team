package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.dto.request.PontuacaoRequest;
import com.example.srvteam.dto.response.PontuacaoResponse;
import com.example.srvteam.mapper.PontuacaoMapper;
import com.example.srvteam.model.Pontuacao;
import com.example.srvteam.repository.PontuacaoRepository;

import jakarta.transaction.Transactional;

@Service
public class PontuacaoService {
    @Autowired
    private PontuacaoRepository pontuacaoRepository;

    @Transactional
    public PontuacaoResponse insPontuacao(PontuacaoRequest request) {
        Pontuacao entity = PontuacaoMapper.toEntity(request);
        Pontuacao saved = pontuacaoRepository.save(entity);
        return PontuacaoMapper.toResponse(saved);
    }

    @Transactional
    public PontuacaoResponse updPontuacao(Integer cdPontuacao, PontuacaoRequest request) {
        Optional<Pontuacao> opt = pontuacaoRepository.findById(cdPontuacao);
        if (opt.isEmpty()) throw new RuntimeException("Pontuação não encontrada");
        Pontuacao entity = opt.get();
        PontuacaoMapper.updateEntity(entity, request);
        Pontuacao updated = pontuacaoRepository.save(entity);
        return PontuacaoMapper.toResponse(updated);
    }

    @Transactional
    public void delPontuacao(Integer cdPontuacao) {
        if (!pontuacaoRepository.existsById(cdPontuacao))
            throw new RuntimeException("Pontuação não encontrada");
        pontuacaoRepository.deleteById(cdPontuacao);
    }

    public List<PontuacaoResponse> listPontuacaoByCdCompeticao(Integer cdCompeticao) {
        List<Pontuacao> list = pontuacaoRepository.findByCdCompeticao(cdCompeticao);
        return list.stream().map(PontuacaoMapper::toResponse).collect(Collectors.toList());
    }
}
