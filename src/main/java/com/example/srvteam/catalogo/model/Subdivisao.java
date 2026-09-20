package com.example.srvteam.catalogo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "tbSubdivisao")
public class Subdivisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdSubdivisao")
    private Integer cdSubdivisao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdPais", nullable = false)
    private Pais pais;

    @NotBlank
    @Size(max = 10)
    @Column(name = "codigoIso", nullable = false, length = 10)
    private String codigoIso;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nmSubdivisao", nullable = false, length = 150)
    private String nmSubdivisao;

    @NotNull
    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;

    @CreationTimestamp
    @Column(name = "dtCadastro", nullable = false, updatable = false)
    private LocalDateTime dtCadastro;

    protected Subdivisao() {
    }

    public Subdivisao(Pais pais, String codigoIso, String nmSubdivisao) {
        this.pais = pais;
        this.codigoIso = codigoIso;
        this.nmSubdivisao = nmSubdivisao;
    }

    public Integer getCdSubdivisao() {
        return cdSubdivisao;
    }

    public Pais getPais() {
        return pais;
    }

    public String getCodigoIso() {
        return codigoIso;
    }

    public String getNmSubdivisao() {
        return nmSubdivisao;
    }

    public Boolean getFlAtivo() {
        return flAtivo;
    }

    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }
}
