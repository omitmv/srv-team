package com.example.srvteam.mapper;

import com.example.srvteam.dto.request.CompetidorRequest;
import com.example.srvteam.dto.response.CompetidorResponse;
import com.example.srvteam.model.Competidores;

public class CompetidorMapper {
    public static Competidores toEntity(CompetidorRequest request) {
        Competidores entity = new Competidores();
        entity.setCdCompetidor(request.getCdCompetidor());
        entity.setCdCompeticao(request.getCdCompeticao());
        // dtCadastro será setado automaticamente
        return entity;
    }

    public static CompetidorResponse toResponse(Competidores entity) {
        CompetidorResponse response = new CompetidorResponse();
        response.setCdCompetidor(entity.getCdCompetidor());
        response.setCdCompeticao(entity.getCdCompeticao());
        response.setDtCadastro(entity.getDtCadastro());
        return response;
    }
}
