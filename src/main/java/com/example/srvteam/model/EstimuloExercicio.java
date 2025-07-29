package com.example.srvteam.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidade EstimuloExercicio
 * Representa a relação entre Estimulo e Exercicio com informações adicionais
 * Tabela: tbEstimuloExercicio
 */
@Entity
@Table(name = "tbEstimuloExercicio")
@IdClass(EstimuloExercicioId.class)
public class EstimuloExercicio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "Código do estímulo é obrigatório")
    @Column(name = "cdEstimulo", nullable = false)
    private Integer cdEstimulo;

    @Id
    @NotNull(message = "Código do exercício é obrigatório")
    @Column(name = "cdExercicio", nullable = false)
    private Integer cdExercicio;

    @Size(max = 250, message = "Série deve ter no máximo 250 caracteres")
    @Column(name = "serie", length = 250)
    private String serie;

    @Size(max = 50, message = "Intervalo deve ter no máximo 50 caracteres")
    @Column(name = "intervalo", length = 50)
    private String intervalo;

    @NotNull(message = "Código da técnica é obrigatório")
    @Column(name = "cdTecnica", nullable = false)
    private Integer cdTecnica;

    @Size(max = 5000, message = "Observação deve ter no máximo 5000 caracteres")
    @Column(name = "obs", length = 5000)
    private String obs;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdEstimulo", insertable = false, updatable = false)
    private Estimulo estimulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdExercicio", insertable = false, updatable = false)
    private Exercicio exercicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdTecnica", insertable = false, updatable = false)
    private Tecnica tecnica;

    // Construtores
    public EstimuloExercicio() {
    }

    public EstimuloExercicio(Integer cdEstimulo, Integer cdExercicio, String serie, 
                           String intervalo, Integer cdTecnica, String obs) {
        this.cdEstimulo = cdEstimulo;
        this.cdExercicio = cdExercicio;
        this.serie = serie;
        this.intervalo = intervalo;
        this.cdTecnica = cdTecnica;
        this.obs = obs;
    }

    // Getters e Setters
    public Integer getCdEstimulo() {
        return cdEstimulo;
    }

    public void setCdEstimulo(Integer cdEstimulo) {
        this.cdEstimulo = cdEstimulo;
    }

    public Integer getCdExercicio() {
        return cdExercicio;
    }

    public void setCdExercicio(Integer cdExercicio) {
        this.cdExercicio = cdExercicio;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(String intervalo) {
        this.intervalo = intervalo;
    }

    public Integer getCdTecnica() {
        return cdTecnica;
    }

    public void setCdTecnica(Integer cdTecnica) {
        this.cdTecnica = cdTecnica;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Estimulo getEstimulo() {
        return estimulo;
    }

    public void setEstimulo(Estimulo estimulo) {
        this.estimulo = estimulo;
    }

    public Exercicio getExercicio() {
        return exercicio;
    }

    public void setExercicio(Exercicio exercicio) {
        this.exercicio = exercicio;
    }

    public Tecnica getTecnica() {
        return tecnica;
    }

    public void setTecnica(Tecnica tecnica) {
        this.tecnica = tecnica;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EstimuloExercicio that = (EstimuloExercicio) obj;
        
        if (!cdEstimulo.equals(that.cdEstimulo)) return false;
        return cdExercicio.equals(that.cdExercicio);
    }

    @Override
    public int hashCode() {
        int result = cdEstimulo.hashCode();
        result = 31 * result + cdExercicio.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EstimuloExercicio{" +
                "cdEstimulo=" + cdEstimulo +
                ", cdExercicio=" + cdExercicio +
                ", serie='" + serie + '\'' +
                ", intervalo='" + intervalo + '\'' +
                ", cdTecnica=" + cdTecnica +
                ", obs='" + obs + '\'' +
                '}';
    }
}
