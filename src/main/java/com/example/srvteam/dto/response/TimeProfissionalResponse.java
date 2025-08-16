package com.example.srvteam.dto.response;

public class TimeProfissionalResponse {

    private Integer cdTime;
    private Integer cdProfissional;
    
    // Dados do Time
    private String nomeTime;
    private String logoTime;
    
    // Dados do Profissional
    private String nomeProfissional;
    private String emailProfissional;
    private Integer cdTpAcesso;
    private String tipoAcesso;

    // Construtores
    public TimeProfissionalResponse() {}

    public TimeProfissionalResponse(Integer cdTime, Integer cdProfissional) {
        this.cdTime = cdTime;
        this.cdProfissional = cdProfissional;
    }

    public TimeProfissionalResponse(Integer cdTime, Integer cdProfissional, 
                                   String nomeTime, String logoTime,
                                   String nomeProfissional, String emailProfissional,
                                   Integer cdTpAcesso, String tipoAcesso) {
        this.cdTime = cdTime;
        this.cdProfissional = cdProfissional;
        this.nomeTime = nomeTime;
        this.logoTime = logoTime;
        this.nomeProfissional = nomeProfissional;
        this.emailProfissional = emailProfissional;
        this.cdTpAcesso = cdTpAcesso;
        this.tipoAcesso = tipoAcesso;
    }

    // Getters e Setters
    public Integer getCdTime() {
        return cdTime;
    }

    public void setCdTime(Integer cdTime) {
        this.cdTime = cdTime;
    }

    public Integer getCdProfissional() {
        return cdProfissional;
    }

    public void setCdProfissional(Integer cdProfissional) {
        this.cdProfissional = cdProfissional;
    }

    public String getNomeTime() {
        return nomeTime;
    }

    public void setNomeTime(String nomeTime) {
        this.nomeTime = nomeTime;
    }

    public String getLogoTime() {
        return logoTime;
    }

    public void setLogoTime(String logoTime) {
        this.logoTime = logoTime;
    }

    public String getNomeProfissional() {
        return nomeProfissional;
    }

    public void setNomeProfissional(String nomeProfissional) {
        this.nomeProfissional = nomeProfissional;
    }

    public String getEmailProfissional() {
        return emailProfissional;
    }

    public void setEmailProfissional(String emailProfissional) {
        this.emailProfissional = emailProfissional;
    }

    public Integer getCdTpAcesso() {
        return cdTpAcesso;
    }

    public void setCdTpAcesso(Integer cdTpAcesso) {
        this.cdTpAcesso = cdTpAcesso;
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }
}
