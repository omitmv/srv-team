package com.example.srvteam.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class FiltroTiposAcessoRequest {
    
    @NotNull(message = "Lista de tipos de acesso não pode ser nula")
    @NotEmpty(message = "Lista de tipos de acesso não pode ser vazia")
    private List<Integer> cdTpAcessos;
    
    private boolean apenasAtivos = true; // Por padrão, busca apenas usuários ativos
    
    // Construtores
    public FiltroTiposAcessoRequest() {}
    
    public FiltroTiposAcessoRequest(List<Integer> cdTpAcessos) {
        this.cdTpAcessos = cdTpAcessos;
    }
    
    public FiltroTiposAcessoRequest(List<Integer> cdTpAcessos, boolean apenasAtivos) {
        this.cdTpAcessos = cdTpAcessos;
        this.apenasAtivos = apenasAtivos;
    }
    
    // Getters e Setters
    public List<Integer> getCdTpAcessos() {
        return cdTpAcessos;
    }
    
    public void setCdTpAcessos(List<Integer> cdTpAcessos) {
        this.cdTpAcessos = cdTpAcessos;
    }
    
    public boolean isApenasAtivos() {
        return apenasAtivos;
    }
    
    public void setApenasAtivos(boolean apenasAtivos) {
        this.apenasAtivos = apenasAtivos;
    }
}
