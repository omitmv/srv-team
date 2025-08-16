package com.example.srvteam.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tbCompeticao")
public class Competicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdCompeticao")
    private Integer cdCompeticao;

    @NotBlank
    @Size(max = 250)
    @Column(name = "nmCompeticao", nullable = false, length = 250)
    private String nmCompeticao;

    @NotNull
    @Column(name = "dtInicio", nullable = false)
    private LocalDate dtInicio;

    @NotNull
    @Column(name = "dtFim", nullable = false)
    private LocalDate dtFim;

    @Size(max = 1000)
    @Column(name = "local", length = 1000)
    private String local;

    @Size(max = 250)
    @Column(name = "federacao", length = 250)
    private String federacao;

    // Getters e Setters
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }

    public String getNmCompeticao() { return nmCompeticao; }
    public void setNmCompeticao(String nmCompeticao) { this.nmCompeticao = nmCompeticao; }

    public LocalDate getDtInicio() { return dtInicio; }
    public void setDtInicio(LocalDate dtInicio) { this.dtInicio = dtInicio; }

    public LocalDate getDtFim() { return dtFim; }
    public void setDtFim(LocalDate dtFim) { this.dtFim = dtFim; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getFederacao() { return federacao; }
    public void setFederacao(String federacao) { this.federacao = federacao; }
}
