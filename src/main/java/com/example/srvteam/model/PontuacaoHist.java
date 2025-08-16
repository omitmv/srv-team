package com.example.srvteam.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbPontuacaoHist")
public class PontuacaoHist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdPontuacaoHist")
    private Integer cdPontuacaoHist;

    @NotNull
    @Column(name = "cdCompetidor", nullable = false)
    private Integer cdCompetidor;

    @NotNull
    @Column(name = "cdCompeticao", nullable = false)
    private Integer cdCompeticao;

    @NotNull
    @Column(name = "cdPontuacao", nullable = false)
    private Integer cdPontuacao;

    @Column(name = "dtCadastro", nullable = false)
    private LocalDateTime dtCadastro = LocalDateTime.now();

    // Getters e Setters
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
