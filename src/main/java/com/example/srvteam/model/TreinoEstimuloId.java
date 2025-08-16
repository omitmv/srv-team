package com.example.srvteam.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe ID composta para TreinoEstimulo
 * Representa a chave primária composta por cdTreino e cdEstimulo
 */
public class TreinoEstimuloId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer cdTreino;
    private Integer cdEstimulo;

    // Construtores
    public TreinoEstimuloId() {
    }

    public TreinoEstimuloId(Integer cdTreino, Integer cdEstimulo) {
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

    // equals e hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TreinoEstimuloId that = (TreinoEstimuloId) obj;
        
        return Objects.equals(cdTreino, that.cdTreino) &&
               Objects.equals(cdEstimulo, that.cdEstimulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdTreino, cdEstimulo);
    }

    @Override
    public String toString() {
        return "TreinoEstimuloId{" +
                "cdTreino=" + cdTreino +
                ", cdEstimulo=" + cdEstimulo +
                '}';
    }
}
