package com.example.srvteam.mapper;

import com.example.srvteam.dto.request.CompeticaoRequest;
import com.example.srvteam.dto.response.CompeticaoResponse;
import com.example.srvteam.model.Competicao;

public class CompeticaoMapper {
    public static Competicao toEntity(CompeticaoRequest request) {
        Competicao entity = new Competicao();
        entity.setNmCompeticao(request.getNmCompeticao());
        entity.setDtInicio(request.getDtInicio());
        entity.setDtFim(request.getDtFim());
        entity.setLocal(request.getLocal());
        entity.setFederacao(request.getFederacao());
        return entity;
    }

    public static void updateEntity(Competicao entity, CompeticaoRequest request) {
        entity.setNmCompeticao(request.getNmCompeticao());
        entity.setDtInicio(request.getDtInicio());
        entity.setDtFim(request.getDtFim());
        entity.setLocal(request.getLocal());
        entity.setFederacao(request.getFederacao());
    }

    public static CompeticaoResponse toResponse(Competicao entity) {
        CompeticaoResponse response = new CompeticaoResponse();
        response.setCdCompeticao(entity.getCdCompeticao());
        response.setNmCompeticao(entity.getNmCompeticao());
        response.setDtInicio(entity.getDtInicio());
        response.setDtFim(entity.getDtFim());
        response.setLocal(entity.getLocal());
        response.setFederacao(entity.getFederacao());
        return response;
    }
}
