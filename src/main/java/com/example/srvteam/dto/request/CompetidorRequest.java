package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;

public class CompetidorRequest {
    @NotNull
    private Integer cdCompetidor;
    @NotNull
    private Integer cdCompeticao;

    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
}
