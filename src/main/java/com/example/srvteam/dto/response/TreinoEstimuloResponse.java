package com.example.srvteam.dto.response;

/**
 * DTO para respostas de consultas de TreinoEstimulo
 */
public class TreinoEstimuloResponse {
    
    private Integer cdTreino;
    private String dsTreino; // Incluído para facilitar a exibição
    
    private Integer cdEstimulo;
    private String dsEstimulo; // Incluído para facilitar a exibição
    
    // Construtores
    public TreinoEstimuloResponse() {}
    
    public TreinoEstimuloResponse(Integer cdTreino, String dsTreino, Integer cdEstimulo, String dsEstimulo) {
        this.cdTreino = cdTreino;
        this.dsTreino = dsTreino;
        this.cdEstimulo = cdEstimulo;
        this.dsEstimulo = dsEstimulo;
    }
    
    // Getters e Setters
    public Integer getCdTreino() {
        return cdTreino;
    }
    
    public void setCdTreino(Integer cdTreino) {
        this.cdTreino = cdTreino;
    }
    
    public String getDsTreino() {
        return dsTreino;
    }
    
    public void setDsTreino(String dsTreino) {
        this.dsTreino = dsTreino;
    }
    
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
}
