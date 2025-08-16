
package com.example.srvteam.dto.response;

import java.math.BigDecimal;

public class PontuacaoResponse {
    private Integer cdPontuacao;
    private Integer cdCompeticao;
    private Integer posicao;
        private BigDecimal pontuacao;

    public Integer getCdPontuacao() { return cdPontuacao; }
    public void setCdPontuacao(Integer cdPontuacao) { this.cdPontuacao = cdPontuacao; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
        public BigDecimal getPontuacao() { return pontuacao; }
        public void setPontuacao(BigDecimal pontuacao) { this.pontuacao = pontuacao; }
}
