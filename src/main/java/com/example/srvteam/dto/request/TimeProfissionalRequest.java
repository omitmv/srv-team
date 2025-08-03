package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;

public class TimeProfissionalRequest {

    @NotNull(message = "Código do time é obrigatório")
    private Integer cdTime;

    @NotNull(message = "Código do profissional é obrigatório")
    private Integer cdProfissional;

    // Construtores
    public TimeProfissionalRequest() {}

    public TimeProfissionalRequest(Integer cdTime, Integer cdProfissional) {
        this.cdTime = cdTime;
        this.cdProfissional = cdProfissional;
    }

    // Getters e Setters
    public Integer getCdTime() {
        return cdTime;
    }

    public void setCdTime(Integer cdTime) {
        this.cdTime = cdTime;
    }

    public Integer getCdProfissional() {
        return cdProfissional;
    }

    public void setCdProfissional(Integer cdProfissional) {
        this.cdProfissional = cdProfissional;
    }
}
