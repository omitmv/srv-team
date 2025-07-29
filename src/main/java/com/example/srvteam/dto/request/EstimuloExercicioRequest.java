package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de EstimuloExercicio
 * Usado para receber dados nas operações de inserção e atualização
 */
public class EstimuloExercicioRequest {

    @NotNull(message = "Código do estímulo é obrigatório")
    private Integer cdEstimulo;

    @NotNull(message = "Código do exercício é obrigatório")
    private Integer cdExercicio;

    @Size(max = 250, message = "Série deve ter no máximo 250 caracteres")
    private String serie;

    @Size(max = 50, message = "Intervalo deve ter no máximo 50 caracteres")
    private String intervalo;

    @NotNull(message = "Código da técnica é obrigatório")
    private Integer cdTecnica;

    @Size(max = 5000, message = "Observação deve ter no máximo 5000 caracteres")
    private String obs;

    // Construtores
    public EstimuloExercicioRequest() {
    }

    public EstimuloExercicioRequest(Integer cdEstimulo, Integer cdExercicio, String serie, 
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

    @Override
    public String toString() {
        return "EstimuloExercicioRequest{" +
                "cdEstimulo=" + cdEstimulo +
                ", cdExercicio=" + cdExercicio +
                ", serie='" + serie + '\'' +
                ", intervalo='" + intervalo + '\'' +
                ", cdTecnica=" + cdTecnica +
                ", obs='" + obs + '\'' +
                '}';
    }
}
