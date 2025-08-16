package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.dto.request.CompeticaoRequest;
import com.example.srvteam.dto.response.CompeticaoResponse;
import com.example.srvteam.mapper.CompeticaoMapper;
import com.example.srvteam.model.Competicao;
import com.example.srvteam.repository.CompeticaoRepository;

import jakarta.transaction.Transactional;

@Service
public class CompeticaoService {
    @Autowired
    private CompeticaoRepository competicaoRepository;

    @Transactional
    public CompeticaoResponse insCompeticao(CompeticaoRequest request) {
        Competicao entity = CompeticaoMapper.toEntity(request);
        Competicao saved = competicaoRepository.save(entity);
        return CompeticaoMapper.toResponse(saved);
    }

    @Transactional
    public CompeticaoResponse updCompeticao(Integer cdCompeticao, CompeticaoRequest request) {
        Optional<Competicao> opt = competicaoRepository.findById(cdCompeticao);
        if (opt.isEmpty()) throw new RuntimeException("Competição não encontrada");
        Competicao entity = opt.get();
        CompeticaoMapper.updateEntity(entity, request);
        Competicao updated = competicaoRepository.save(entity);
        return CompeticaoMapper.toResponse(updated);
    }

    @Transactional
    public void delCompeticao(Integer cdCompeticao) {
        if (!competicaoRepository.existsById(cdCompeticao))
            throw new RuntimeException("Competição não encontrada");
        competicaoRepository.deleteById(cdCompeticao);
    }

    public List<CompeticaoResponse> listCompeticaoByNmCompeticao(String nmCompeticao) {
        List<Competicao> list = competicaoRepository.buscarPorNomeLike(nmCompeticao);
        return list.stream().map(CompeticaoMapper::toResponse).collect(Collectors.toList());
    }
}
