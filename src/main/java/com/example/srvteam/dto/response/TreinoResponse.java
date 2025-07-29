package com.example.srvteam.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para respostas de consultas de Treino
 */
public class TreinoResponse {
    
    private Integer cdTreino;
    private String dsTreino;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtCadastro;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtInicio;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtFinal;
    
    private Integer cdProfissional;
    private String nomeProfissional; // Incluído para facilitar a exibição
    
    private Integer cdAtleta;
    private String nomeAtleta; // Incluído para facilitar a exibição
    
    private String obs;
    
    // Construtores
    public TreinoResponse() {}
    
    public TreinoResponse(Integer cdTreino, String dsTreino, LocalDateTime dtCadastro,
                         LocalDateTime dtInicio, LocalDateTime dtFinal, Integer cdProfissional,
                         String nomeProfissional, Integer cdAtleta, String nomeAtleta, String obs) {
        this.cdTreino = cdTreino;
        this.dsTreino = dsTreino;
        this.dtCadastro = dtCadastro;
        this.dtInicio = dtInicio;
        this.dtFinal = dtFinal;
        this.cdProfissional = cdProfissional;
        this.nomeProfissional = nomeProfissional;
        this.cdAtleta = cdAtleta;
        this.nomeAtleta = nomeAtleta;
        this.obs = obs;
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
    
    public String getNomeProfissional() {
        return nomeProfissional;
    }
    
    public void setNomeProfissional(String nomeProfissional) {
        this.nomeProfissional = nomeProfissional;
    }
    
    public Integer getCdAtleta() {
        return cdAtleta;
    }
    
    public void setCdAtleta(Integer cdAtleta) {
        this.cdAtleta = cdAtleta;
    }
    
    public String getNomeAtleta() {
        return nomeAtleta;
    }
    
    public void setNomeAtleta(String nomeAtleta) {
        this.nomeAtleta = nomeAtleta;
    }
    
    public String getObs() {
        return obs;
    }
    
    public void setObs(String obs) {
        this.obs = obs;
    }
}
