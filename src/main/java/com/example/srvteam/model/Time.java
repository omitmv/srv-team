package com.example.srvteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tbTime")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdTime")
    private Integer cdTime;

    @NotBlank(message = "Nome do time é obrigatório")
    @Size(max = 250, message = "Nome do time não pode exceder 250 caracteres")
    @Column(name = "dsNome", nullable = false, length = 250)
    private String dsNome;

    @Size(max = 500, message = "Logo não pode exceder 500 caracteres")
    @Column(name = "logo", length = 500)
    private String logo;

    @NotNull(message = "Profissional responsável é obrigatório")
    @Column(name = "cdProfissionalResponsavel", nullable = false)
    private Integer cdProfissionalResponsavel;

    // Relacionamento com Usuario (Profissional Responsável)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdProfissionalResponsavel", insertable = false, updatable = false)
    private Usuario profissionalResponsavel;

    // Construtores
    public Time() {}

    public Time(String dsNome, String logo, Integer cdProfissionalResponsavel) {
        this.dsNome = dsNome;
        this.logo = logo;
        this.cdProfissionalResponsavel = cdProfissionalResponsavel;
    }

    // Getters e Setters
    public Integer getCdTime() {
        return cdTime;
    }

    public void setCdTime(Integer cdTime) {
        this.cdTime = cdTime;
    }

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

    public Usuario getProfissionalResponsavel() {
        return profissionalResponsavel;
    }

    public void setProfissionalResponsavel(Usuario profissionalResponsavel) {
        this.profissionalResponsavel = profissionalResponsavel;
    }

    @Override
    public String toString() {
        return "Time{" +
                "cdTime=" + cdTime +
                ", dsNome='" + dsNome + '\'' +
                ", logo='" + logo + '\'' +
                ", cdProfissionalResponsavel=" + cdProfissionalResponsavel +
                '}';
    }
}
