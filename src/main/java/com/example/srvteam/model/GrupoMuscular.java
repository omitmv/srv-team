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
 * Entidade GrupoMuscular
 * Representa um grupo muscular no banco de dados
 */
@Entity
@Table(name = "tbGrupoMuscular")
public class GrupoMuscular {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdGrupoMuscular")
    private Integer cdGrupoMuscular;
    
    @NotBlank(message = "Descrição do grupo muscular é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsGrupoMuscular", unique = true, nullable = false, length = 250)
    private String dsGrupoMuscular;
    
    // Construtor padrão
    public GrupoMuscular() {}
    
    // Construtor com parâmetros
    public GrupoMuscular(String dsGrupoMuscular) {
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    // Getters e Setters
    public Integer getCdGrupoMuscular() {
        return cdGrupoMuscular;
    }
    
    public void setCdGrupoMuscular(Integer cdGrupoMuscular) {
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public String getDsGrupoMuscular() {
        return dsGrupoMuscular;
    }
    
    public void setDsGrupoMuscular(String dsGrupoMuscular) {
        this.dsGrupoMuscular = dsGrupoMuscular;
    }
    
    @Override
    public String toString() {
        return "GrupoMuscular{" +
                "cdGrupoMuscular=" + cdGrupoMuscular +
                ", dsGrupoMuscular='" + dsGrupoMuscular + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        GrupoMuscular that = (GrupoMuscular) o;
        
        if (cdGrupoMuscular != null ? !cdGrupoMuscular.equals(that.cdGrupoMuscular) : that.cdGrupoMuscular != null) return false;
        return dsGrupoMuscular != null ? dsGrupoMuscular.equals(that.dsGrupoMuscular) : that.dsGrupoMuscular == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdGrupoMuscular != null ? cdGrupoMuscular.hashCode() : 0;
        result = 31 * result + (dsGrupoMuscular != null ? dsGrupoMuscular.hashCode() : 0);
        return result;
    }
}
