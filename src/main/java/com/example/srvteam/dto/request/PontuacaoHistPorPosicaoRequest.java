package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;

public class PontuacaoHistPorPosicaoRequest {
    @NotNull
    private Integer cdCompetidor;
    @NotNull
    private Integer cdCompeticao;
    @NotNull
    private Integer posicao;

    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
}
