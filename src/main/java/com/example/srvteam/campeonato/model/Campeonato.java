package com.example.srvteam.campeonato.model;

import com.example.srvteam.catalogo.model.NomeCatalogoNormalizer;
import com.example.srvteam.catalogo.model.Organizador;
import com.example.srvteam.catalogo.model.Pais;
import com.example.srvteam.catalogo.model.Subdivisao;
import com.example.srvteam.model.Usuario;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tbCompeticao")
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdCompeticao")
    private Integer cdCompeticao;

    @NotBlank
    @Size(max = 250)
    @Column(name = "nmCompeticao", nullable = false, length = 250)
    private String nmCompeticao;

    @Column(name = "nmCompeticaoNormalizado", nullable = false, length = 250)
    private String nmCompeticaoNormalizado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdOrganizador", nullable = false)
    private Organizador organizador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdPais", nullable = false)
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cdSubdivisao")
    private Subdivisao subdivisao;

    @Size(max = 1000)
    @Column(name = "local", length = 1000)
    private String local;

    @NotNull
    @Column(name = "dtInicio", nullable = false)
    private LocalDate dtInicio;

    @NotNull
    @Column(name = "dtFim", nullable = false)
    private LocalDate dtFim;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampeonatoStatus status = CampeonatoStatus.ATIVO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdCriador", nullable = false)
    private Usuario criador;

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

    @Version
    @Column(name = "lockVersion", nullable = false)
    private Long lockVersion;

    protected Campeonato() {
    }

    public Campeonato(
            String nmCompeticao,
            Organizador organizador,
            Pais pais,
            Subdivisao subdivisao,
            String local,
            LocalDate dtInicio,
            LocalDate dtFim,
            Usuario criador,
            Integer cdUsuarioCadastro) {
        setNmCompeticao(nmCompeticao);
        this.organizador = Objects.requireNonNull(organizador, "Organizador é obrigatório");
        this.pais = Objects.requireNonNull(pais, "País é obrigatório");
        this.subdivisao = subdivisao;
        this.local = local;
        this.dtInicio = Objects.requireNonNull(dtInicio, "Data de início é obrigatória");
        this.dtFim = Objects.requireNonNull(dtFim, "Data de fim é obrigatória");
        this.criador = Objects.requireNonNull(criador, "Criador é obrigatório");
        this.cdUsuarioCadastro = Objects.requireNonNull(cdUsuarioCadastro, "Usuário de cadastro é obrigatório");
        this.status = CampeonatoStatus.ATIVO;
        validateInvariants();
    }

    @PrePersist
    @PreUpdate
    private void beforeSave() {
        this.nmCompeticaoNormalizado = NomeCatalogoNormalizer.normalize(this.nmCompeticao);
        validateInvariants();
    }

    private void validateInvariants() {
        if (dtFim != null && dtInicio != null && dtFim.isBefore(dtInicio)) {
            throw new IllegalArgumentException("dtFim não pode ser anterior a dtInicio");
        }
        if (!Boolean.TRUE.equals(organizador.getFlAtivo())) {
            throw new IllegalArgumentException("Organizador inativo não pode ser utilizado");
        }
        if (!Boolean.TRUE.equals(pais.getFlAtivo())) {
            throw new IllegalArgumentException("País inativo não pode ser utilizado");
        }
        if (subdivisao != null) {
            if (!Boolean.TRUE.equals(subdivisao.getFlAtivo())) {
                throw new IllegalArgumentException("Subdivisão inativa não pode ser utilizada");
            }
            Integer cdPaisCampeonato = pais.getCdPais();
            Integer cdPaisSubdivisao = subdivisao.getPais().getCdPais();
            if (cdPaisCampeonato != null && cdPaisSubdivisao != null
                    && !Objects.equals(cdPaisCampeonato, cdPaisSubdivisao)) {
                throw new IllegalArgumentException("Subdivisão deve pertencer ao país informado");
            }
        }
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
    }

    public Integer getCdCompeticao() {
        return cdCompeticao;
    }

    public String getNmCompeticao() {
        return nmCompeticao;
    }

    public void setNmCompeticao(String nmCompeticao) {
        this.nmCompeticao = nmCompeticao;
        this.nmCompeticaoNormalizado = NomeCatalogoNormalizer.normalize(nmCompeticao);
    }

    public String getNmCompeticaoNormalizado() {
        return nmCompeticaoNormalizado;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public Pais getPais() {
        return pais;
    }

    public Subdivisao getSubdivisao() {
        return subdivisao;
    }

    public String getLocal() {
        return local;
    }

    public LocalDate getDtInicio() {
        return dtInicio;
    }

    public LocalDate getDtFim() {
        return dtFim;
    }

    public CampeonatoStatus getStatus() {
        return status;
    }

    public void cancelar(Integer cdUsuarioAlteracao) {
        this.status = CampeonatoStatus.CANCELADO;
        this.cdUsuarioAlteracao = cdUsuarioAlteracao;
    }

    public void ativar(Integer cdUsuarioAlteracao) {
        this.status = CampeonatoStatus.ATIVO;
        this.cdUsuarioAlteracao = cdUsuarioAlteracao;
    }

    public Usuario getCriador() {
        return criador;
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

    public Long getLockVersion() {
        return lockVersion;
    }
}
