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
 * Entidade Estimulo
 * Representa um estímulo no banco de dados
 */
@Entity
@Table(name = "tbEstimulo")
public class Estimulo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdEstimulo")
    private Integer cdEstimulo;
    
    @NotBlank(message = "Descrição do estímulo é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsEstimulo", unique = true, nullable = false, length = 250)
    private String dsEstimulo;
    
    @Size(max = 5000, message = "Observação não pode exceder 5000 caracteres")
    @Column(name = "obs", length = 5000)
    private String obs;
    
    // Construtor padrão
    public Estimulo() {}
    
    // Construtor com parâmetros
    public Estimulo(String dsEstimulo) {
        this.dsEstimulo = dsEstimulo;
    }
    
    public Estimulo(String dsEstimulo, String obs) {
        this.dsEstimulo = dsEstimulo;
        this.obs = obs;
    }
    
    // Getters e Setters
    public Integer getCdEstimulo() {
        return cdEstimulo;
    }
    
    public void setCdEstimulo(Integer cdEstimulo) {
        this.cdEstimulo = cdEstimulo;
    }
    
    public String getDsEstimulo() {
        return dsEstimulo;
    }
    
    public void setDsEstimulo(String dsEstimulo) {
        this.dsEstimulo = dsEstimulo;
    }
    
    public String getObs() {
        return obs;
    }
    
    public void setObs(String obs) {
        this.obs = obs;
    }
    
    @Override
    public String toString() {
        return "Estimulo{" +
                "cdEstimulo=" + cdEstimulo +
                ", dsEstimulo='" + dsEstimulo + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Estimulo estimulo = (Estimulo) o;
        
        if (cdEstimulo != null ? !cdEstimulo.equals(estimulo.cdEstimulo) : estimulo.cdEstimulo != null) return false;
        return dsEstimulo != null ? dsEstimulo.equals(estimulo.dsEstimulo) : estimulo.dsEstimulo == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdEstimulo != null ? cdEstimulo.hashCode() : 0;
        result = 31 * result + (dsEstimulo != null ? dsEstimulo.hashCode() : 0);
        return result;
    }
}
