package com.example.srvteam.dto.response;

public class TimeResponse {

    private Integer cdTime;
    private String dsNome;
    private String logo;
    private Integer cdProfissionalResponsavel;
    private String nomeProfissionalResponsavel;
    private String emailProfissionalResponsavel;

    // Construtores
    public TimeResponse() {}

    public TimeResponse(Integer cdTime, String dsNome, String logo, Integer cdProfissionalResponsavel) {
        this.cdTime = cdTime;
        this.dsNome = dsNome;
        this.logo = logo;
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
    }

    public TimeResponse(Integer cdTime, String dsNome, String logo, Integer cdProfissionalResponsavel, 
                       String nomeProfissionalResponsavel, String emailProfissionalResponsavel) {
        this.cdTime = cdTime;
        this.dsNome = dsNome;
        this.logo = logo;
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
        this.nomeProfissionalResponsavel = nomeProfissionalResponsavel;
        this.emailProfissionalResponsavel = emailProfissionalResponsavel;
    }

    // Getters e Setters
    public Integer getCdTime() {
        return cdTime;
    }

    public void setCdTime(Integer cdTime) {
        this.cdTime = cdTime;
    }

    public String getDsNome() {
        return dsNome;
    }

    public void setDsNome(String dsNome) {
        this.dsNome = dsNome;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getCdProfissionalResponsavel() {
        return cdProfissionalResponsavel;
    }

    public void setCdProfissionalResponsavel(Integer cdProfissionalResponsavel) {
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
    }

    public String getNomeProfissionalResponsavel() {
        return nomeProfissionalResponsavel;
    }

    public void setNomeProfissionalResponsavel(String nomeProfissionalResponsavel) {
        this.nomeProfissionalResponsavel = nomeProfissionalResponsavel;
    }

    public String getEmailProfissionalResponsavel() {
        return emailProfissionalResponsavel;
    }

    public void setEmailProfissionalResponsavel(String emailProfissionalResponsavel) {
        this.emailProfissionalResponsavel = emailProfissionalResponsavel;
    }
}
