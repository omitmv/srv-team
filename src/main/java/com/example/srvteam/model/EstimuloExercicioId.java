package com.example.srvteam.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe ID composta para EstimuloExercicio
 * Representa a chave primária composta por cdEstimulo e cdExercicio
 */
public class EstimuloExercicioId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer cdEstimulo;
    private Integer cdExercicio;

    // Construtores
    public EstimuloExercicioId() {
    }

    public EstimuloExercicioId(Integer cdEstimulo, Integer cdExercicio) {
        this.cdEstimulo = cdEstimulo;
        this.cdExercicio = cdExercicio;
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

    // equals e hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EstimuloExercicioId that = (EstimuloExercicioId) obj;
        
        return Objects.equals(cdEstimulo, that.cdEstimulo) && 
               Objects.equals(cdExercicio, that.cdExercicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdEstimulo, cdExercicio);
    }

    @Override
    public String toString() {
        return "EstimuloExercicioId{" +
                "cdEstimulo=" + cdEstimulo +
                ", cdExercicio=" + cdExercicio +
                '}';
    }
}
