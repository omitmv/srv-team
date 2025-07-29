package com.example.srvteam.dto.response;

/**
 * DTO para respostas de consultas de GrupoMuscular
 */
public class GrupoMuscularResponse {
    
    private Integer cdGrupoMuscular;
    private String dsGrupoMuscular;
    
    // Construtores
    public GrupoMuscularResponse() {}
    
    public GrupoMuscularResponse(Integer cdGrupoMuscular, String dsGrupoMuscular) {
        this.cdGrupoMuscular = cdGrupoMuscular;
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    // Getters e Setters
    public Integer getCdGrupoMuscular() {
        return cdGrupoMuscular;
    }
    
    public void setCdGrupoMuscular(Integer cdGrupoMuscular) {
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public String getDsGrupoMuscular() {
        return dsGrupoMuscular;
    }
    
    public void setDsGrupoMuscular(String dsGrupoMuscular) {
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    @Override
    public String toString() {
        return "GrupoMuscularResponse{" +
                "cdGrupoMuscular=" + cdGrupoMuscular +
                ", dsGrupoMuscular='" + dsGrupoMuscular + '\'' +
                '}';
    }
}
