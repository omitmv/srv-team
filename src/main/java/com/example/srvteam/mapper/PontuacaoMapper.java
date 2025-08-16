package com.example.srvteam.mapper;

import com.example.srvteam.dto.request.PontuacaoRequest;
import com.example.srvteam.dto.response.PontuacaoResponse;
import com.example.srvteam.model.Pontuacao;

public class PontuacaoMapper {
    public static Pontuacao toEntity(PontuacaoRequest request) {
        Pontuacao entity = new Pontuacao();
        entity.setCdCompeticao(request.getCdCompeticao());
        entity.setPosicao(request.getPosicao());
        entity.setPontuacao(request.getPontuacao());
        return entity;
    }

    public static void updateEntity(Pontuacao entity, PontuacaoRequest request) {
        entity.setCdCompeticao(request.getCdCompeticao());
        entity.setPosicao(request.getPosicao());
        entity.setPontuacao(request.getPontuacao());
    }

    public static PontuacaoResponse toResponse(Pontuacao entity) {
        PontuacaoResponse response = new PontuacaoResponse();
        response.setCdPontuacao(entity.getCdPontuacao());
        response.setCdCompeticao(entity.getCdCompeticao());
        response.setPosicao(entity.getPosicao());
        response.setPontuacao(entity.getPontuacao());
        return response;
    }
}
