package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisições de criação de TreinoEstimulo
 */
public class TreinoEstimuloRequest {
    
    @NotNull(message = "Código do treino é obrigatório")
    private Integer cdTreino;
    
    @NotNull(message = "Código do estímulo é obrigatório")
    private Integer cdEstimulo;
    
    // Construtores
    public TreinoEstimuloRequest() {}
    
    public TreinoEstimuloRequest(Integer cdTreino, Integer cdEstimulo) {
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
}
