package com.example.srvteam.dto;

/**
 * DTO para respostas de Sistema
 */
public class SistemaResponse {
    
    private Integer cdSistema;
    private String dsSistema;
    private Boolean flAtivo;
    
    // Construtores
    public SistemaResponse() {}
    
    public SistemaResponse(Integer cdSistema, String dsSistema, Boolean flAtivo) {
        this.cdSistema = cdSistema;
        this.dsSistema = dsSistema;
        this.flAtivo = flAtivo;
    }
    
    // Getters e Setters
    public Integer getCdSistema() {
        return cdSistema;
    }
    
    public void setCdSistema(Integer cdSistema) {
        this.cdSistema = cdSistema;
    }
    
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
        return "SistemaResponse{" +
                "cdSistema=" + cdSistema +
                ", dsSistema='" + dsSistema + '\'' +
                ", flAtivo=" + flAtivo +
                '}';
    }
}
