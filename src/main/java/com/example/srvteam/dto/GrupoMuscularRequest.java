package com.example.srvteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de GrupoMuscular
 */
public class GrupoMuscularRequest {
    
    @NotBlank(message = "Descrição do grupo muscular é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsGrupoMuscular;
    
    // Construtores
    public GrupoMuscularRequest() {}
    
    public GrupoMuscularRequest(String dsGrupoMuscular) {
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    // Getters e Setters
    public String getDsGrupoMuscular() {
        return dsGrupoMuscular;
    }
    
    public void setDsGrupoMuscular(String dsGrupoMuscular) {
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    @Override
    public String toString() {
        return "GrupoMuscularRequest{" +
                "dsGrupoMuscular='" + dsGrupoMuscular + '\'' +
                '}';
    }
}
