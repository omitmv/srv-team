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

/**
 * Entidade TreinoEstimulo
 * Representa a relação entre Treino e Estimulo
 * Tabela: tbTreinoEstimulo
 */
@Entity
@Table(name = "tbTreinoEstimulo")
@IdClass(TreinoEstimuloId.class)
public class TreinoEstimulo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "Código do treino é obrigatório")
    @Column(name = "cdTreino", nullable = false)
    private Integer cdTreino;

    @Id
    @NotNull(message = "Código do estímulo é obrigatório")
    @Column(name = "cdEstimulo", nullable = false)
    private Integer cdEstimulo;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdTreino", insertable = false, updatable = false)
    private Treino treino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdEstimulo", insertable = false, updatable = false)
    private Estimulo estimulo;

    // Construtores
    public TreinoEstimulo() {
    }

    public TreinoEstimulo(Integer cdTreino, Integer cdEstimulo) {
        this.cdTreino = cdTreino;
        this.cdEstimulo = cdEstimulo;
    }

    // Getters e Setters
    public Integer getCdTreino() {
        return cdTreino;
    }

    public void setCdTreino(Integer cdTreino) {
        this.cdTreino = cdTreino;
    }

    public Integer getCdEstimulo() {
        return cdEstimulo;
    }

    public void setCdEstimulo(Integer cdEstimulo) {
        this.cdEstimulo = cdEstimulo;
    }

    public Treino getTreino() {
        return treino;
    }

    public void setTreino(Treino treino) {
        this.treino = treino;
    }

    public Estimulo getEstimulo() {
        return estimulo;
    }

    public void setEstimulo(Estimulo estimulo) {
        this.estimulo = estimulo;
    }

    // toString
    @Override
    public String toString() {
        return "TreinoEstimulo{" +
                "cdTreino=" + cdTreino +
                ", cdEstimulo=" + cdEstimulo +
                '}';
    }

    // equals e hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TreinoEstimulo that = (TreinoEstimulo) obj;
        
        return java.util.Objects.equals(cdTreino, that.cdTreino) &&
               java.util.Objects.equals(cdEstimulo, that.cdEstimulo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cdTreino, cdEstimulo);
    }
}
