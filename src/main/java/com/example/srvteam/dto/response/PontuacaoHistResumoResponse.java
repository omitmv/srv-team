package com.example.srvteam.dto.response;

import java.math.BigDecimal;

public class PontuacaoHistResumoResponse {
    private String nome;
    private String nmCompeticao;
    private BigDecimal totalPontuacao;

    public PontuacaoHistResumoResponse() {}

    public PontuacaoHistResumoResponse(String nome, String nmCompeticao, BigDecimal totalPontuacao) {
        this.nome = nome;
        this.nmCompeticao = nmCompeticao;
        this.totalPontuacao = totalPontuacao;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNmCompeticao() { return nmCompeticao; }
    public void setNmCompeticao(String nmCompeticao) { this.nmCompeticao = nmCompeticao; }
    public BigDecimal getTotalPontuacao() { return totalPontuacao; }
    public void setTotalPontuacao(BigDecimal totalPontuacao) { this.totalPontuacao = totalPontuacao; }
}
