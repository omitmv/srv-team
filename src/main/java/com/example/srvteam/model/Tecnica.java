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
 * Entidade Tecnica
 * Representa uma técnica no banco de dados
 */
@Entity
@Table(name = "tbTecnica")
public class Tecnica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdTecnica")
    private Integer cdTecnica;
    
    @NotBlank(message = "Descrição da técnica é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsTecnica", unique = true, nullable = false, length = 250)
    private String dsTecnica;
    
    @Size(max = 500, message = "Observação não pode exceder 500 caracteres")
    @Column(name = "obs", length = 500)
    private String obs;
    
    // Construtor padrão
    public Tecnica() {}
    
    // Construtor com parâmetros
    public Tecnica(String dsTecnica) {
        this.dsTecnica = dsTecnica;
    }
    
    public Tecnica(String dsTecnica, String obs) {
        this.dsTecnica = dsTecnica;
        this.obs = obs;
    }
    
    // Getters e Setters
    public Integer getCdTecnica() {
        return cdTecnica;
    }
    
    public void setCdTecnica(Integer cdTecnica) {
        this.cdTecnica = cdTecnica;
    }
    
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
        return "Tecnica{" +
                "cdTecnica=" + cdTecnica +
                ", dsTecnica='" + dsTecnica + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Tecnica tecnica = (Tecnica) o;
        
        if (cdTecnica != null ? !cdTecnica.equals(tecnica.cdTecnica) : tecnica.cdTecnica != null) return false;
        return dsTecnica != null ? dsTecnica.equals(tecnica.dsTecnica) : tecnica.dsTecnica == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdTecnica != null ? cdTecnica.hashCode() : 0;
        result = 31 * result + (dsTecnica != null ? dsTecnica.hashCode() : 0);
        return result;
    }
}
