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
 * Entidade Exercicio
 * Representa um exercício no banco de dados
 */
@Entity
@Table(name = "tbExercicio")
public class Exercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdExercicio")
    private Integer cdExercicio;
    
    @NotBlank(message = "Descrição do exercício é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsExercicio", unique = true, nullable = false, length = 250)
    private String dsExercicio;
    
    @NotNull(message = "Grupo muscular é obrigatório")
    @Column(name = "cdGrupoMuscular", nullable = false)
    private Integer cdGrupoMuscular;
    
    @Size(max = 500, message = "URL do vídeo não pode exceder 500 caracteres")
    @Column(name = "video", length = 500)
    private String video;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdGrupoMuscular", insertable = false, updatable = false)
    private GrupoMuscular grupoMuscular;
    
    // Construtor padrão
    public Exercicio() {}
    
    // Construtor com parâmetros
    public Exercicio(String dsExercicio, Integer cdGrupoMuscular) {
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public Exercicio(String dsExercicio, Integer cdGrupoMuscular, String video) {
        this.dsExercicio = dsExercicio;
        this.cdGrupoMuscular = cdGrupoMuscular;
        this.video = video;
    }
    
    // Getters e Setters
    public Integer getCdExercicio() {
        return cdExercicio;
    }
    
    public void setCdExercicio(Integer cdExercicio) {
        this.cdExercicio = cdExercicio;
    }
    
    public String getDsExercicio() {
        return dsExercicio;
    }
    
    public void setDsExercicio(String dsExercicio) {
        this.dsExercicio = dsExercicio;
    }
    
    public Integer getCdGrupoMuscular() {
        return cdGrupoMuscular;
    }
    
    public void setCdGrupoMuscular(Integer cdGrupoMuscular) {
        this.cdGrupoMuscular = cdGrupoMuscular;
    }
    
    public String getVideo() {
        return video;
    }
    
    public void setVideo(String video) {
        this.video = video;
    }
    
    public GrupoMuscular getGrupoMuscular() {
        return grupoMuscular;
    }
    
    public void setGrupoMuscular(GrupoMuscular grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }
    
    @Override
    public String toString() {
        return "Exercicio{" +
                "cdExercicio=" + cdExercicio +
                ", dsExercicio='" + dsExercicio + '\'' +
                ", cdGrupoMuscular=" + cdGrupoMuscular +
                ", video='" + video + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Exercicio exercicio = (Exercicio) o;
        
        if (cdExercicio != null ? !cdExercicio.equals(exercicio.cdExercicio) : exercicio.cdExercicio != null) return false;
        return dsExercicio != null ? dsExercicio.equals(exercicio.dsExercicio) : exercicio.dsExercicio == null;
    }
    
    @Override
    public int hashCode() {
        int result = cdExercicio != null ? cdExercicio.hashCode() : 0;
        result = 31 * result + (dsExercicio != null ? dsExercicio.hashCode() : 0);
        return result;
    }
}
