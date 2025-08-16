package com.example.srvteam.dto.response;

import java.time.LocalDate;

public class CompeticaoResponse {
    private Integer cdCompeticao;
    private String nmCompeticao;
    private LocalDate dtInicio;
    private LocalDate dtFim;
    private String local;
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
