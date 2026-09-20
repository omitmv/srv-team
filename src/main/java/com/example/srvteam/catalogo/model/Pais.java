package com.example.srvteam.catalogo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tbPais")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdPais")
    private Integer cdPais;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "codigoIso2", nullable = false, length = 2)
    private String codigoIso2;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "codigoIso3", nullable = false, length = 3)
    private String codigoIso3;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nmPais", nullable = false, length = 150)
    private String nmPais;

    @NotNull
    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;

    @CreationTimestamp
    @Column(name = "dtCadastro", nullable = false, updatable = false)
    private LocalDateTime dtCadastro;

    protected Pais() {
    }

    public Pais(String codigoIso2, String codigoIso3, String nmPais) {
        this.codigoIso2 = codigoIso2;
        this.codigoIso3 = codigoIso3;
        this.nmPais = nmPais;
    }

    public Integer getCdPais() {
        return cdPais;
    }

    public String getCodigoIso2() {
        return codigoIso2;
    }

    public String getCodigoIso3() {
        return codigoIso3;
    }

    public String getNmPais() {
        return nmPais;
    }

    public Boolean getFlAtivo() {
        return flAtivo;
    }

    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }
}
