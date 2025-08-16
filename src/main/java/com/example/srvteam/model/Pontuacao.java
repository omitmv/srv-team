package com.example.srvteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbPontuacao")
public class Pontuacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdPontuacao")
    private Integer cdPontuacao;

    @NotNull
    @Column(name = "cdCompeticao", nullable = false)
    private Integer cdCompeticao;

    @NotNull
    @Column(name = "posicao", nullable = false)
    private Integer posicao;

    @NotNull
    @Column(name = "pontuacao", nullable = false)
    private Integer pontuacao = 0;

    // Getters e Setters
    public Integer getCdPontuacao() { return cdPontuacao; }
    public void setCdPontuacao(Integer cdPontuacao) { this.cdPontuacao = cdPontuacao; }

    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }

    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }

    public Integer getPontuacao() { return pontuacao; }
    public void setPontuacao(Integer pontuacao) { this.pontuacao = pontuacao; }
}
