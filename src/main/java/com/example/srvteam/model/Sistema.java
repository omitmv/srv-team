package com.example.srvteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entidade Sistema
 * Representa um sistema no banco de dados
 */
@Entity
@Table(name = "tbSistema")
public class Sistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdSistema")
    private Integer cdSistema;
    
    @NotBlank(message = "Descrição do sistema é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsSistema", unique = true, nullable = false, length = 250)
    private String dsSistema;
    
    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;
    
    // Construtores
    public Sistema() {}
    
    public Sistema(String dsSistema) {
        this.dsSistema = dsSistema;
        this.flAtivo = true;
    }
    
    public Sistema(String dsSistema, Boolean flAtivo) {
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
    
    // Métodos utilitários
    @Override
    public String toString() {
        return "Sistema{" +
                "cdSistema=" + cdSistema +
                ", dsSistema='" + dsSistema + '\'' +
                ", flAtivo=" + flAtivo +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Sistema sistema = (Sistema) o;
        
        if (cdSistema != null ? !cdSistema.equals(sistema.cdSistema) : sistema.cdSistema != null) return false;
        return dsSistema != null ? dsSistema.equals(sistema.dsSistema) : sistema.dsSistema == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdSistema != null ? cdSistema.hashCode() : 0;
        result = 31 * result + (dsSistema != null ? dsSistema.hashCode() : 0);
        return result;
    }
}
