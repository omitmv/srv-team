package com.example.srvteam.util;

import com.example.srvteam.dto.request.TimeRequest;
import com.example.srvteam.dto.response.TimeResponse;
import com.example.srvteam.model.Time;

public class TimeMapper {

    /**
     * Converte TimeRequest para Time
     */
    public static Time toEntity(TimeRequest request) {
        if (request == null) {
            return null;
        }

        Time time = new Time();
        time.setDsNome(request.getDsNome());
        time.setLogo(request.getLogo());
        time.setCdProfissionalResponsavel(request.getCdProfissionalResponsavel());

        return time;
    }

    /**
     * Converte Time para TimeResponse
     */
    public static TimeResponse toResponse(Time time) {
        if (time == null) {
            return null;
        }

        TimeResponse response = new TimeResponse(
                time.getCdTime(),
                time.getDsNome(),
                time.getLogo(),
                time.getCdProfissionalResponsavel()
        );

        // Se o profissional responsável estiver carregado, incluir suas informações
        if (time.getProfissionalResponsavel() != null) {
            response.setNomeProfissionalResponsavel(time.getProfissionalResponsavel().getNome());
            response.setEmailProfissionalResponsavel(time.getProfissionalResponsavel().getEmail());
        }

        return response;
    }

    /**
     * Atualiza um Time existente com dados do TimeRequest
     */
    public static void updateEntity(Time time, TimeRequest request) {
        if (time == null || request == null) {
            return;
        }

        time.setDsNome(request.getDsNome());
        time.setLogo(request.getLogo());
        time.setCdProfissionalResponsavel(request.getCdProfissionalResponsavel());
    }
}
