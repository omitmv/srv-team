package com.example.srvteam.dto;

/**
 * DTO para respostas de consultas de Menu
 */
public class MenuResponse {
    
    private Integer cdMenu;
    private String dsMenu;
    private Integer cdSistema;
    private String dsSistema; // Incluído para facilitar a exibição
    
    // Construtores
    public MenuResponse() {}
    
    public MenuResponse(Integer cdMenu, String dsMenu, Integer cdSistema) {
        this.cdMenu = cdMenu;
        this.dsMenu = dsMenu;
        this.cdSistema = cdSistema;
    }
    
    public MenuResponse(Integer cdMenu, String dsMenu, Integer cdSistema, String dsSistema) {
        this.cdMenu = cdMenu;
        this.dsMenu = dsMenu;
        this.cdSistema = cdSistema;
        this.dsSistema = dsSistema;
    }
    
    // Getters e Setters
    public Integer getCdMenu() {
        return cdMenu;
    }
    
    public void setCdMenu(Integer cdMenu) {
        this.cdMenu = cdMenu;
    }
    
    public String getDsMenu() {
        return dsMenu;
    }
    
    public void setDsMenu(String dsMenu) {
        this.dsMenu = dsMenu;
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
    
    @Override
    public String toString() {
        return "MenuResponse{" +
                "cdMenu=" + cdMenu +
                ", dsMenu='" + dsMenu + '\'' +
                ", cdSistema=" + cdSistema +
                ", dsSistema='" + dsSistema + '\'' +
                '}';
    }
}
