package com.example.srvteam.catalogo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tbClasse")
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdClasse")
    private Integer cdClasse;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdCategoria", nullable = false)
    private Categoria categoria;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nmClasse", nullable = false, length = 150)
    private String nmClasse;

    @Column(name = "nmClasseNormalizado", nullable = false, length = 150)
    private String nmClasseNormalizado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoClasse", nullable = false, length = 20)
    private TipoClasse tipoClasse;

    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;

    @CreationTimestamp
    @Column(name = "dtCadastro", nullable = false, updatable = false)
    private LocalDateTime dtCadastro;

    @Column(name = "cdUsuarioCadastro", nullable = false)
    private Integer cdUsuarioCadastro;

    @UpdateTimestamp
    @Column(name = "dtAlteracao")
    private LocalDateTime dtAlteracao;

    @Column(name = "cdUsuarioAlteracao")
    private Integer cdUsuarioAlteracao;

    protected Classe() {
    }

    public Classe(Categoria categoria, String nmClasse, TipoClasse tipoClasse, Integer cdUsuarioCadastro) {
        this.categoria = categoria;
        setNmClasse(nmClasse);
        this.tipoClasse = tipoClasse;
        this.cdUsuarioCadastro = cdUsuarioCadastro;
    }

    public Integer getCdClasse() {
        return cdClasse;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getNmClasse() {
        return nmClasse;
    }

    public void setNmClasse(String nmClasse) {
        this.nmClasse = nmClasse;
        this.nmClasseNormalizado = NomeCatalogoNormalizer.normalize(nmClasse);
    }

    public String getNmClasseNormalizado() {
        return nmClasseNormalizado;
    }

    public TipoClasse getTipoClasse() {
        return tipoClasse;
    }

    public Boolean getFlAtivo() {
        return flAtivo;
    }

    public void setFlAtivo(Boolean flAtivo) {
        this.flAtivo = flAtivo;
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

    public void setCdUsuarioAlteracao(Integer cdUsuarioAlteracao) {
        this.cdUsuarioAlteracao = cdUsuarioAlteracao;
    }
}
