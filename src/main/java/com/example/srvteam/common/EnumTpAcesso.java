package com.example.srvteam.common;

public enum EnumTpAcesso {
    ADMINISTRADOR(1, "Administrador"),
    NUTRITIONISTA(2, "Nutricionista"),
    TREINADOR(3, "Treinador"),
    COACH(4, "Coach"),
    FUNCIONARIO(5, "Funcionário"),
    ATLETA(6, "Atleta");

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
