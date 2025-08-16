package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompeticaoBuscarRequest {
    @NotBlank(message = "Nome da competição é obrigatório")
    @Size(max = 250, message = "Nome da competição não pode exceder 250 caracteres")
    private String nome;

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}