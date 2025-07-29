package com.example.srvteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Tecnica
 */
public class TecnicaRequest {
    
    @NotBlank(message = "Descrição da técnica é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsTecnica;
    
    @Size(max = 500, message = "Observação não pode exceder 500 caracteres")
    private String obs;
    
    // Construtores
    public TecnicaRequest() {}
    
    public TecnicaRequest(String dsTecnica) {
        this.dsTecnica = dsTecnica;
    }
    
    public TecnicaRequest(String dsTecnica, String obs) {
        this.dsTecnica = dsTecnica;
        this.obs = obs;
    }
    
    // Getters e Setters
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
        return "TecnicaRequest{" +
                "dsTecnica='" + dsTecnica + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
