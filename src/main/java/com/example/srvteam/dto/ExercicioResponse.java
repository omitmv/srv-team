package com.example.srvteam.dto;

/**
 * DTO para respostas de consultas de Exercicio
 */
public class ExercicioResponse {
    
    private Integer cdExercicio;
    private String dsExercicio;
    private Integer cdGrupoMuscular;
    private String dsGrupoMuscular; // Incluído para facilitar a exibição
    private String video;
    
    // Construtores
    public ExercicioResponse() {}
    
    public ExercicioResponse(Integer cdExercicio, String dsExercicio, Integer cdGrupoMuscular) {
        this.cdExercicio = cdExercicio;
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public ExercicioResponse(Integer cdExercicio, String dsExercicio, Integer cdGrupoMuscular, String dsGrupoMuscular) {
        this.cdExercicio = cdExercicio;
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    public ExercicioResponse(Integer cdExercicio, String dsExercicio, Integer cdGrupoMuscular, String dsGrupoMuscular, String video) {
        this.cdExercicio = cdExercicio;
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
        this.dsGrupoMuscular = dsGrupoMuscular;
        this.video = video;
    }
    
    // Getters e Setters
    public Integer getCdExercicio() {
        return cdExercicio;
    }
    
    public void setCdExercicio(Integer cdExercicio) {
        this.cdExercicio = cdExercicio;
    }
    
    public String getDsExercicio() {
        return dsExercicio;
    }
    
    public void setDsExercicio(String dsExercicio) {
        this.dsExercicio = dsExercicio;
    }
    
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
    
    public String getVideo() {
        return video;
    }
    
    public void setVideo(String video) {
        this.video = video;
    }
    
    @Override
    public String toString() {
        return "ExercicioResponse{" +
                "cdExercicio=" + cdExercicio +
                ", dsExercicio='" + dsExercicio + '\'' +
                ", cdGrupoMuscular=" + cdGrupoMuscular +
                ", dsGrupoMuscular='" + dsGrupoMuscular + '\'' +
                ", video='" + video + '\'' +
                '}';
    }
}
