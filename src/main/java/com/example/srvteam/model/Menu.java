package com.example.srvteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidade Menu
 * Representa um menu no banco de dados
 */
@Entity
@Table(name = "tbMenu")
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdMenu")
    private Integer cdMenu;
    
    @NotBlank(message = "Descrição do menu é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsMenu", unique = true, nullable = false, length = 250)
    private String dsMenu;
    
    @NotNull(message = "Sistema é obrigatório")
    @Column(name = "cdSistema", nullable = false)
    private Integer cdSistema;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdSistema", insertable = false, updatable = false)
    private Sistema sistema;
    
    // Construtor padrão
    public Menu() {}
    
    // Construtor com parâmetros
    public Menu(String dsMenu, Integer cdSistema) {
        this.dsMenu = dsMenu;
        this.cdSistema = cdSistema;
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
    
    public Sistema getSistema() {
        return sistema;
    }
    
    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }
    
    @Override
    public String toString() {
        return "Menu{" +
                "cdMenu=" + cdMenu +
                ", dsMenu='" + dsMenu + '\'' +
                ", cdSistema=" + cdSistema +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Menu menu = (Menu) o;
        
        if (cdMenu != null ? !cdMenu.equals(menu.cdMenu) : menu.cdMenu != null) return false;
        return dsMenu != null ? dsMenu.equals(menu.dsMenu) : menu.dsMenu == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdMenu != null ? cdMenu.hashCode() : 0;
        result = 31 * result + (dsMenu != null ? dsMenu.hashCode() : 0);
        return result;
    }
}
