package com.example.srvteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Sistema
 */
public class SistemaRequest {
    
    @NotBlank(message = "Descrição do sistema é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsSistema;
    
    private Boolean flAtivo;
    
    // Construtores
    public SistemaRequest() {}
    
    public SistemaRequest(String dsSistema) {
        this.dsSistema = dsSistema;
    }
    
    public SistemaRequest(String dsSistema, Boolean flAtivo) {
        this.dsSistema = dsSistema;
        this.flAtivo = flAtivo;
    }
    
    // Getters e Setters
    public String getDsSistema() {
        return dsSistema;
    }
    
    public void setDsSistema(String dsSistema) {
        this.dsSistema = dsSistema;
    }
    
    public Boolean getFlAtivo() {
        return flAtivo;
    }
    
    public void setFlAtivo(Boolean flAtivo) {
        this.flAtivo = flAtivo;
    }
    
    @Override
    public String toString() {
        return "SistemaRequest{" +
                "dsSistema='" + dsSistema + '\'' +
                ", flAtivo=" + flAtivo +
                '}';
    }
}
