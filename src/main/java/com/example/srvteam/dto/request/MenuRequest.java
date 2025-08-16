package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Menu
 */
public class MenuRequest {
    
    @NotBlank(message = "Descrição do menu é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsMenu;
    
    @NotNull(message = "Sistema é obrigatório")
    private Integer cdSistema;
    
    // Construtores
    public MenuRequest() {}
    
    public MenuRequest(String dsMenu, Integer cdSistema) {
        this.dsMenu = dsMenu;
        this.cdSistema = cdSistema;
    }
    
    // Getters e Setters
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
    
    @Override
    public String toString() {
        return "MenuRequest{" +
                "dsMenu='" + dsMenu + '\'' +
                ", cdSistema=" + cdSistema +
                '}';
    }
}
