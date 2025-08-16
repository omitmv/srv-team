package com.example.srvteam.dto.response;

public class MenuResponse {
    
    private Integer cdMenu;
    private String dsMenu;
    private Integer cdSistema;
    private String dsSistema;
    
    public MenuResponse() {
    }
    
    public MenuResponse(Integer cdMenu, String dsMenu, Integer cdSistema, String dsSistema) {
        this.cdMenu = cdMenu;
        this.dsMenu = dsMenu;
        this.cdSistema = cdSistema;
        this.dsSistema = dsSistema;
    }
    
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
}
