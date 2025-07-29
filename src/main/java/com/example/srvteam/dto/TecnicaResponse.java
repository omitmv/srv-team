package com.example.srvteam.dto;

/**
 * DTO para respostas de consultas de Tecnica
 */
public class TecnicaResponse {
    
    private Integer cdTecnica;
    private String dsTecnica;
    private String obs;
    
    // Construtores
    public TecnicaResponse() {}
    
    public TecnicaResponse(Integer cdTecnica, String dsTecnica) {
        this.cdTecnica = cdTecnica;
        this.dsTecnica = dsTecnica;
    }
    
    public TecnicaResponse(Integer cdTecnica, String dsTecnica, String obs) {
        this.cdTecnica = cdTecnica;
        this.dsTecnica = dsTecnica;
        this.obs = obs;
    }
    
    // Getters e Setters
    public Integer getCdTecnica() {
        return cdTecnica;
    }
    
    public void setCdTecnica(Integer cdTecnica) {
        this.cdTecnica = cdTecnica;
    }
    
    public String getDsTecnica() {
        return dsTecnica;
    }
    
    public void setDsTecnica(String dsTecnica) {
        this.dsTecnica = dsTecnica;
    }
    
    public String getObs() {
        return obs;
    }
    
    public void setObs(String obs) {
        this.obs = obs;
    }
    
    @Override
    public String toString() {
        return "TecnicaResponse{" +
                "cdTecnica=" + cdTecnica +
                ", dsTecnica='" + dsTecnica + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
