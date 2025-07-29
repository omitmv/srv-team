package com.example.srvteam.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
 * Entidade Treino
 * Representa um treino no banco de dados
 * Tabela: tbTreino
 */
@Entity
@Table(name = "tbTreino")
public class Treino {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdTreino")
    private Integer cdTreino;
    
    @NotBlank(message = "Descrição do treino é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    @Column(name = "dsTreino", unique = true, nullable = false, length = 250)
    private String dsTreino;
    
    @CreationTimestamp
    @Column(name = "dtCadastro", nullable = false, updatable = false)
    private LocalDateTime dtCadastro;
    
    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "dtInicio", nullable = false)
    private LocalDateTime dtInicio;
    
    @NotNull(message = "Data final é obrigatória")
    @Column(name = "dtFinal", nullable = false)
    private LocalDateTime dtFinal;
    
    @NotNull(message = "Profissional é obrigatório")
    @Column(name = "cdProfissional", nullable = false)
    private Integer cdProfissional;
    
    @NotNull(message = "Atleta é obrigatório")
    @Column(name = "cdAtleta", nullable = false)
    private Integer cdAtleta;
    
    @Size(max = 2500, message = "Observação não pode exceder 2500 caracteres")
    @Column(name = "obs", length = 2500)
    private String obs;
    
    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdProfissional", insertable = false, updatable = false)
    private Usuario profissional;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdAtleta", insertable = false, updatable = false)
    private Usuario atleta;
    
    // Construtores
    public Treino() {
    }
    
    public Treino(String dsTreino, LocalDateTime dtInicio, LocalDateTime dtFinal, 
                  Integer cdProfissional, Integer cdAtleta) {
        this.dsTreino = dsTreino;
        this.dtInicio = dtInicio;
        this.dtFinal = dtFinal;
        this.cdProfissional = cdProfissional;
        this.cdAtleta = cdAtleta;
    }
    
    // Getters e Setters
    public Integer getCdTreino() {
        return cdTreino;
    }
    
    public void setCdTreino(Integer cdTreino) {
        this.cdTreino = cdTreino;
    }
    
    public String getDsTreino() {
        return dsTreino;
    }
    
    public void setDsTreino(String dsTreino) {
        this.dsTreino = dsTreino;
    }
    
    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }
    
    public void setDtCadastro(LocalDateTime dtCadastro) {
        this.dtCadastro = dtCadastro;
    }
    
    public LocalDateTime getDtInicio() {
        return dtInicio;
    }
    
    public void setDtInicio(LocalDateTime dtInicio) {
        this.dtInicio = dtInicio;
    }
    
    public LocalDateTime getDtFinal() {
        return dtFinal;
    }
    
    public void setDtFinal(LocalDateTime dtFinal) {
        this.dtFinal = dtFinal;
    }
    
    public Integer getCdProfissional() {
        return cdProfissional;
    }
    
    public void setCdProfissional(Integer cdProfissional) {
        this.cdProfissional = cdProfissional;
    }
    
    public Integer getCdAtleta() {
        return cdAtleta;
    }
    
    public void setCdAtleta(Integer cdAtleta) {
        this.cdAtleta = cdAtleta;
    }
    
    public String getObs() {
        return obs;
    }
    
    public void setObs(String obs) {
        this.obs = obs;
    }
    
    public Usuario getProfissional() {
        return profissional;
    }
    
    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }
    
    public Usuario getAtleta() {
        return atleta;
    }
    
    public void setAtleta(Usuario atleta) {
        this.atleta = atleta;
    }
    
    // toString
    @Override
    public String toString() {
        return "Treino{" +
                "cdTreino=" + cdTreino +
                ", dsTreino='" + dsTreino + '\'' +
                ", dtCadastro=" + dtCadastro +
                ", dtInicio=" + dtInicio +
                ", dtFinal=" + dtFinal +
                ", cdProfissional=" + cdProfissional +
                ", cdAtleta=" + cdAtleta +
                ", obs='" + obs + '\'' +
                '}';
    }
    
    // equals e hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Treino treino = (Treino) obj;
        
        return cdTreino != null ? cdTreino.equals(treino.cdTreino) : treino.cdTreino == null;
    }
    
    @Override
    public int hashCode() {
        return cdTreino != null ? cdTreino.hashCode() : 0;
    }
}
