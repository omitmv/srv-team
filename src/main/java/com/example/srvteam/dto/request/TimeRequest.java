package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TimeRequest {

    @NotBlank(message = "Nome do time é obrigatório")
    @Size(max = 250, message = "Nome do time não pode exceder 250 caracteres")
    private String dsNome;

    @Size(max = 500, message = "Logo não pode exceder 500 caracteres")
    private String logo;

    @NotNull(message = "Profissional responsável é obrigatório")
    private Integer cdProfissionalResponsavel;

    // Construtores
    public TimeRequest() {}

    public TimeRequest(String dsNome, String logo, Integer cdProfissionalResponsavel) {
        this.dsNome = dsNome;
        this.logo = logo;
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
    }

    // Getters e Setters
    public String getDsNome() {
        return dsNome;
    }

    public void setDsNome(String dsNome) {
        this.dsNome = dsNome;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getCdProfissionalResponsavel() {
        return cdProfissionalResponsavel;
    }

    public void setCdProfissionalResponsavel(Integer cdProfissionalResponsavel) {
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
    }
}
