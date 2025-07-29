package com.example.srvteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Estimulo
 */
public class EstimuloRequest {
    
    @NotBlank(message = "Descrição do estímulo é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsEstimulo;
    
    @Size(max = 5000, message = "Observação não pode exceder 5000 caracteres")
    private String obs;
    
    // Construtores
    public EstimuloRequest() {}
    
    public EstimuloRequest(String dsEstimulo) {
        this.dsEstimulo = dsEstimulo;
    }
    
    public EstimuloRequest(String dsEstimulo, String obs) {
        this.dsEstimulo = dsEstimulo;
        this.obs = obs;
    }
    
    // Getters e Setters
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
        return "EstimuloRequest{" +
                "dsEstimulo='" + dsEstimulo + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
