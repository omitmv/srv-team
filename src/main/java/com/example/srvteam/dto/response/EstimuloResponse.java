package com.example.srvteam.dto.response;

/**
 * DTO para respostas de consultas de Estimulo
 */
public class EstimuloResponse {
    
    private Integer cdEstimulo;
    private String dsEstimulo;
    private String obs;
    
    // Construtores
    public EstimuloResponse() {}
    
    public EstimuloResponse(Integer cdEstimulo, String dsEstimulo) {
        this.cdEstimulo = cdEstimulo;
        this.dsEstimulo = dsEstimulo;
    }
    
    public EstimuloResponse(Integer cdEstimulo, String dsEstimulo, String obs) {
        this.cdEstimulo = cdEstimulo;
        this.dsEstimulo = dsEstimulo;
        this.obs = obs;
    }
    
    // Getters e Setters
    public Integer getCdEstimulo() {
        return cdEstimulo;
    }
    
    public void setCdEstimulo(Integer cdEstimulo) {
        this.cdEstimulo = cdEstimulo;
    }
    
    public String getDsEstimulo() {
        return dsEstimulo;
    }
    
    public void setDsEstimulo(String dsEstimulo) {
        this.dsEstimulo = dsEstimulo;
    }
    
    public String getObs() {
        return obs;
    }
    
    public void setObs(String obs) {
        this.obs = obs;
    }
    
    @Override
    public String toString() {
        return "EstimuloResponse{" +
                "cdEstimulo=" + cdEstimulo +
                ", dsEstimulo='" + dsEstimulo + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
