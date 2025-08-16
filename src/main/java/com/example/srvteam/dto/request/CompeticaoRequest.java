package com.example.srvteam.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CompeticaoRequest {
    @NotBlank
    @Size(max = 250)
    private String nmCompeticao;

    @NotNull
    private LocalDate dtInicio;

    @NotNull
    private LocalDate dtFim;

    @Size(max = 1000)
    private String local;

    @Size(max = 250)
    private String federacao;

    // Getters e Setters
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
