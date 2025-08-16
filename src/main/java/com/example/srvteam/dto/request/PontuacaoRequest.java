
package com.example.srvteam.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public class PontuacaoRequest {
    @NotNull
    private Integer cdCompeticao;
    @NotNull
    private Integer posicao;
        @NotNull
        private BigDecimal pontuacao;

    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
        public BigDecimal getPontuacao() { return pontuacao; }
        public void setPontuacao(BigDecimal pontuacao) { this.pontuacao = pontuacao; }
}
