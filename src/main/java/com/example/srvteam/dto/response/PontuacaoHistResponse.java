package com.example.srvteam.dto.response;

import java.time.LocalDateTime;

public class PontuacaoHistResponse {
    private Integer cdPontuacaoHist;
    private Integer cdCompetidor;
    private Integer cdCompeticao;
    private Integer cdPontuacao;
    private LocalDateTime dtCadastro;

    public Integer getCdPontuacaoHist() { return cdPontuacaoHist; }
    public void setCdPontuacaoHist(Integer cdPontuacaoHist) { this.cdPontuacaoHist = cdPontuacaoHist; }
    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getCdPontuacao() { return cdPontuacao; }
    public void setCdPontuacao(Integer cdPontuacao) { this.cdPontuacao = cdPontuacao; }
    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }
}
