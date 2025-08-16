package com.example.srvteam.dto.response;

public class SistemaResponse {
    
    private Integer cdSistema;
    private String dsSistema;
    private Boolean flAtivo;
    
    public SistemaResponse() {
    }
    
    public SistemaResponse(Integer cdSistema, String dsSistema, Boolean flAtivo) {
        this.cdSistema = cdSistema;
        this.dsSistema = dsSistema;
        this.flAtivo = flAtivo;
    }
    
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
}
