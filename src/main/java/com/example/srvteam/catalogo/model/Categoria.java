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
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tbCategoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdCategoria")
    private Integer cdCategoria;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nmCategoria", nullable = false, length = 150)
    private String nmCategoria;

    @Column(name = "nmCategoriaNormalizado", nullable = false, length = 150)
    private String nmCategoriaNormalizado;

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

    protected Categoria() {
    }

    public Categoria(String nmCategoria, Integer cdUsuarioCadastro) {
        setNmCategoria(nmCategoria);
        this.cdUsuarioCadastro = cdUsuarioCadastro;
    }

    public Integer getCdCategoria() {
        return cdCategoria;
    }

    public String getNmCategoria() {
        return nmCategoria;
    }

    public void setNmCategoria(String nmCategoria) {
        this.nmCategoria = nmCategoria;
        this.nmCategoriaNormalizado = NomeCatalogoNormalizer.normalize(nmCategoria);
    }

    public String getNmCategoriaNormalizado() {
        return nmCategoriaNormalizado;
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
