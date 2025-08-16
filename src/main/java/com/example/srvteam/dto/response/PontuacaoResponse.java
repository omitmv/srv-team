package com.example.srvteam.dto.response;

public class PontuacaoResponse {
    private Integer cdPontuacao;
    private Integer cdCompeticao;
    private Integer posicao;
    private Integer pontuacao;

    public Integer getCdPontuacao() { return cdPontuacao; }
    public void setCdPontuacao(Integer cdPontuacao) { this.cdPontuacao = cdPontuacao; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
    public Integer getPontuacao() { return pontuacao; }
    public void setPontuacao(Integer pontuacao) { this.pontuacao = pontuacao; }
}
