package com.example.srvteam.catalogo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "tbOrganizador")
public class Organizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdOrganizador")
    private Integer cdOrganizador;

    @NotBlank
    @Size(max = 250)
    @Column(name = "nmOrganizador", nullable = false, length = 250)
    private String nmOrganizador;

    @Column(name = "nmOrganizadorNormalizado", nullable = false, length = 250)
    private String nmOrganizadorNormalizado;

    @NotNull
    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;

    @CreationTimestamp
    @Column(name = "dtCadastro", nullable = false, updatable = false)
    private LocalDateTime dtCadastro;

    @NotNull
    @Column(name = "cdUsuarioCadastro", nullable = false)
    private Integer cdUsuarioCadastro;

    @UpdateTimestamp
    @Column(name = "dtAlteracao")
    private LocalDateTime dtAlteracao;

    @Column(name = "cdUsuarioAlteracao")
    private Integer cdUsuarioAlteracao;

    protected Organizador() {
    }

    public Organizador(String nmOrganizador, Integer cdUsuarioCadastro) {
        setNmOrganizador(nmOrganizador);
        this.cdUsuarioCadastro = cdUsuarioCadastro;
    }

    public Integer getCdOrganizador() {
        return cdOrganizador;
    }

    public String getNmOrganizador() {
        return nmOrganizador;
    }

    public void setNmOrganizador(String nmOrganizador) {
        this.nmOrganizador = nmOrganizador;
        this.nmOrganizadorNormalizado = NomeCatalogoNormalizer.normalize(nmOrganizador);
    }

    public String getNmOrganizadorNormalizado() {
        return nmOrganizadorNormalizado;
    }

    public Boolean getFlAtivo() {
        return flAtivo;
    }

    public void inativar(Integer cdUsuarioAlteracao) {
        this.flAtivo = false;
        this.cdUsuarioAlteracao = cdUsuarioAlteracao;
    }

    public void reativar(Integer cdUsuarioAlteracao) {
        this.flAtivo = true;
        this.cdUsuarioAlteracao = cdUsuarioAlteracao;
    }

    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }

    public Integer getCdUsuarioCadastro() {
        return cdUsuarioCadastro;
    }

    public LocalDateTime getDtAlteracao() {
        return dtAlteracao;
    }

    public Integer getCdUsuarioAlteracao() {
        return cdUsuarioAlteracao;
    }
}
