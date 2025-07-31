package com.example.srvteam.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioRequest {

    @NotBlank(message = "Login é obrigatório")
    @Size(max = 250, message = "Login não pode exceder 250 caracteres")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 250, message = "Nome não pode exceder 250 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 250, message = "Email não pode exceder 250 caracteres")
    private String email;

    @NotNull(message = "Tipo de acesso é obrigatório")
    private Integer cdTpAcesso;

    private Boolean flAtivo;
    private LocalDateTime dtExpiracao;

    // Construtores
    public UsuarioRequest() {}

    public UsuarioRequest(String login, String senha, String nome, String email, Integer cdTpAcesso) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.cdTpAcesso = cdTpAcesso;
    }

    // Getters e Setters
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

    public Integer getCdTpAcesso() {
        return cdTpAcesso;
    }

    public void setCdTpAcesso(Integer cdTpAcesso) {
        this.cdTpAcesso = cdTpAcesso;
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
}
