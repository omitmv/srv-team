package com.example.srvteam.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação e atualização de Treino
 */
public class TreinoRequest {
    
    @NotBlank(message = "Descrição do treino é obrigatória")
    @Size(max = 250, message = "Descrição não pode exceder 250 caracteres")
    private String dsTreino;
    
    @NotNull(message = "Data de início é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dtInicio;
    
    @NotNull(message = "Data final é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dtFinal;
    
    @NotNull(message = "Profissional é obrigatório")
    private Integer cdProfissional;
    
    @NotNull(message = "Atleta é obrigatório")
    private Integer cdAtleta;
    
    @Size(max = 2500, message = "Observação não pode exceder 2500 caracteres")
    private String obs;
    
    // Construtores
    public TreinoRequest() {}
    
    public TreinoRequest(String dsTreino, LocalDate dtInicio, LocalDate dtFinal, 
                        Integer cdProfissional, Integer cdAtleta) {
        this.dsTreino = dsTreino;
        this.dtInicio = dtInicio;
        this.dtFinal = dtFinal;
        this.cdProfissional = cdProfissional;
        this.cdAtleta = cdAtleta;
    }
    
    public TreinoRequest(String dsTreino, LocalDate dtInicio, LocalDate dtFinal, 
                        Integer cdProfissional, Integer cdAtleta, String obs) {
        this.dsTreino = dsTreino;
        this.dtInicio = dtInicio;
        this.dtFinal = dtFinal;
        this.cdProfissional = cdProfissional;
        this.cdAtleta = cdAtleta;
        this.obs = obs;
    }
    
    // Getters e Setters
    public String getDsTreino() {
        return dsTreino;
    }
    
    public void setDsTreino(String dsTreino) {
        this.dsTreino = dsTreino;
    }
    
    public LocalDate getDtInicio() {
        return dtInicio;
    }
    
    public void setDtInicio(LocalDate dtInicio) {
        this.dtInicio = dtInicio;
    }
    
    public LocalDate getDtFinal() {
        return dtFinal;
    }
    
    public void setDtFinal(LocalDate dtFinal) {
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
}
