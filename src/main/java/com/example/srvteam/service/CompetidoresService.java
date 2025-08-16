package com.example.srvteam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.dto.request.CompetidorRequest;
import com.example.srvteam.dto.response.CompetidorResponse;
import com.example.srvteam.mapper.CompetidorMapper;
import com.example.srvteam.model.Competidores;
import com.example.srvteam.model.CompetidoresId;
import com.example.srvteam.repository.CompetidoresRepository;

import jakarta.transaction.Transactional;

@Service
public class CompetidoresService {
    @Autowired
    private CompetidoresRepository competidoresRepository;

    @Transactional
    public CompetidorResponse insCompetidor(CompetidorRequest request) {
        Competidores entity = CompetidorMapper.toEntity(request);
        Competidores saved = competidoresRepository.save(entity);
        return CompetidorMapper.toResponse(saved);
    }

    @Transactional
    public void delCompetidor(Integer cdCompetidor, Integer cdCompeticao) {
        CompetidoresId id = new CompetidoresId(cdCompetidor, cdCompeticao);
        if (!competidoresRepository.existsById(id))
            throw new RuntimeException("Competidor não encontrado nesta competição");
        competidoresRepository.deleteById(id);
    }

    public List<CompetidorResponse> listCompetidorByCdCompeticao(Integer cdCompeticao) {
        List<Competidores> list = competidoresRepository.findByCdCompeticao(cdCompeticao);
        return list.stream().map(CompetidorMapper::toResponse).collect(Collectors.toList());
    }
}
