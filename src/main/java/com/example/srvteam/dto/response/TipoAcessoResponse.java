package com.example.srvteam.dto.response;

public class TipoAcessoResponse {

    private Integer codigo;
    private String descricao;

    // Construtores
    public TipoAcessoResponse() {}

    public TipoAcessoResponse(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
