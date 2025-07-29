package com.example.srvteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Exercicio
 */
public class ExercicioRequest {
    
    @NotBlank(message = "Descrição do exercício é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsExercicio;
    
    @NotNull(message = "Grupo muscular é obrigatório")
    private Integer cdGrupoMuscular;
    
    @Size(max = 500, message = "URL do vídeo não pode exceder 500 caracteres")
    private String video;
    
    // Construtores
    public ExercicioRequest() {}
    
    public ExercicioRequest(String dsExercicio, Integer cdGrupoMuscular) {
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public ExercicioRequest(String dsExercicio, Integer cdGrupoMuscular, String video) {
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
        this.video = video;
    }
    
    // Getters e Setters
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
    
    public String getVideo() {
        return video;
    }
    
    public void setVideo(String video) {
        this.video = video;
    }
    
    @Override
    public String toString() {
        return "ExercicioRequest{" +
                "dsExercicio='" + dsExercicio + '\'' +
                ", cdGrupoMuscular=" + cdGrupoMuscular +
                ", video='" + video + '\'' +
                '}';
    }
}
