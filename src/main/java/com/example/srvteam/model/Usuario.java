package com.example.srvteam.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.srvteam.common.EnumTpAcesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tbUsuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdUsuario")
    private Integer cdUsuario;
    
    @NotBlank(message = "Login é obrigatório")
    @Size(max = 250, message = "Login não pode exceder 250 caracteres")
    @Column(unique = true, nullable = false, length = 250)
    private String login;
    
    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String senha;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 250, message = "Nome não pode exceder 250 caracteres")
    @Column(nullable = false, length = 250)
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 250, message = "Email não pode exceder 250 caracteres")
    @Column(unique = true, nullable = false, length = 250)
    private String email;
    
    @CreationTimestamp
    @Column(name = "dataCadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
    
    @Column(name = "flAtivo", nullable = false)
    private Boolean flAtivo = true;
    
    @Column(name = "dtExpiracao")
    private LocalDateTime dtExpiracao;
    
    @NotNull(message = "Tipo de acesso é obrigatório")
    @Column(name = "cdTpAcesso", nullable = false)
    private Integer cdTpAcesso;
    
    // Construtores
    public Usuario() {}
    
    public Usuario(String login, String senha, String nome, String email) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.flAtivo = true;
        this.cdTpAcesso = EnumTpAcesso.ATLETA.getCodigo(); // Valor padrão
    }
    
    public Usuario(String login, String senha, String nome, String email, Integer cdTpAcesso) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.flAtivo = true;
        this.cdTpAcesso = cdTpAcesso;
    }
    
    // Getters e Setters
    public Integer getCdUsuario() {
        return cdUsuario;
    }
    
    public void setCdUsuario(Integer cdUsuario) {
        this.cdUsuario = cdUsuario;
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Boolean getFlAtivo() {
        return flAtivo;
    }
    
    public void setFlAtivo(Boolean flAtivo) {
        this.flAtivo = flAtivo;
    }
    
    public LocalDateTime getDtExpiracao() {
        return dtExpiracao;
    }
    
    public void setDtExpiracao(LocalDateTime dtExpiracao) {
        this.dtExpiracao = dtExpiracao;
    }
    
    public Integer getCdTpAcesso() {
        return cdTpAcesso;
    }
    
    public void setCdTpAcesso(Integer cdTpAcesso) {
        this.cdTpAcesso = cdTpAcesso;
    }
    
    public EnumTpAcesso getTipoAcesso() {
        if (cdTpAcesso == null) return null;
        for (EnumTpAcesso tipo : EnumTpAcesso.values()) {
            if (tipo.getCodigo() == cdTpAcesso) {
                return tipo;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "cdUsuario=" + cdUsuario +
                ", login='" + login + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", dataCadastro=" + dataCadastro +
                ", flAtivo=" + flAtivo +
                ", dtExpiracao=" + dtExpiracao +
                ", cdTpAcesso=" + cdTpAcesso +
                '}';
    }
}
