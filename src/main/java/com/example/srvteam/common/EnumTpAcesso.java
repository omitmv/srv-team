package com.example.srvteam.common;

public enum EnumTpAcesso {
    ADMINISTRADOR(1, "Administrador"),
    ATLETA(2, "Atleta"),
    TREINADOR(3, "Treinador"),
    NUTRITIONISTA(4, "Nutricionista"),
    COACH(5, "Coach");

    private final int codigo;
    private final String descricao;

    EnumTpAcesso(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
