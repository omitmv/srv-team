package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PontuacaoHistRequest {
    @NotNull
    private Integer cdCompetidor;
    @NotNull
    private Integer cdCompeticao;
    @NotNull
    private Integer cdPontuacao;
    private LocalDateTime dtCadastro;

    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getCdPontuacao() { return cdPontuacao; }
    public void setCdPontuacao(Integer cdPontuacao) { this.cdPontuacao = cdPontuacao; }
    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }
}
