package com.example.srvteam.mapper;

import com.example.srvteam.dto.request.PontuacaoHistRequest;
import com.example.srvteam.dto.response.PontuacaoHistResponse;
import com.example.srvteam.model.PontuacaoHist;

public class PontuacaoHistMapper {
    public static PontuacaoHist toEntity(PontuacaoHistRequest request) {
        PontuacaoHist entity = new PontuacaoHist();
        entity.setCdCompetidor(request.getCdCompetidor());
        entity.setCdCompeticao(request.getCdCompeticao());
        entity.setCdPontuacao(request.getCdPontuacao());
        if (request.getDtCadastro() != null) {
            entity.setDtCadastro(request.getDtCadastro());
        }
        return entity;
    }

    public static void updateEntity(PontuacaoHist entity, PontuacaoHistRequest request) {
        entity.setCdCompetidor(request.getCdCompetidor());
        entity.setCdCompeticao(request.getCdCompeticao());
        entity.setCdPontuacao(request.getCdPontuacao());
        if (request.getDtCadastro() != null) {
            entity.setDtCadastro(request.getDtCadastro());
        }
    }

    public static PontuacaoHistResponse toResponse(PontuacaoHist entity) {
        PontuacaoHistResponse response = new PontuacaoHistResponse();
        response.setCdPontuacaoHist(entity.getCdPontuacaoHist());
        response.setCdCompetidor(entity.getCdCompetidor());
        response.setCdCompeticao(entity.getCdCompeticao());
        response.setCdPontuacao(entity.getCdPontuacao());
        response.setDtCadastro(entity.getDtCadastro());
        return response;
    }
}
