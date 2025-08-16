package com.example.srvteam.dto.response;

/**
 * DTO para resposta de EstimuloExercicio
 * Usado para retornar dados nas operações de consulta
 */
public class EstimuloExercicioResponse {

    private Integer cdEstimulo;
    private String dsEstimulo;
    private Integer cdExercicio;
    private String dsExercicio;
    private String serie;
    private String intervalo;
    private Integer cdTecnica;
    private String dsTecnica;
    private String obs;

    // Construtores
    public EstimuloExercicioResponse() {
    }

    public EstimuloExercicioResponse(Integer cdEstimulo, String dsEstimulo, Integer cdExercicio, 
                                   String dsExercicio, String serie, String intervalo, 
                                   Integer cdTecnica, String dsTecnica, String obs) {
        this.cdEstimulo = cdEstimulo;
        this.dsEstimulo = dsEstimulo;
        this.cdExercicio = cdExercicio;
        this.dsExercicio = dsExercicio;
        this.serie = serie;
        this.intervalo = intervalo;
        this.cdTecnica = cdTecnica;
        this.dsTecnica = dsTecnica;
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
        return "EstimuloExercicioResponse{" +
                "cdEstimulo=" + cdEstimulo +
                ", dsEstimulo='" + dsEstimulo + '\'' +
                ", cdExercicio=" + cdExercicio +
                ", dsExercicio='" + dsExercicio + '\'' +
                ", serie='" + serie + '\'' +
                ", intervalo='" + intervalo + '\'' +
                ", cdTecnica=" + cdTecnica +
                ", dsTecnica='" + dsTecnica + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
