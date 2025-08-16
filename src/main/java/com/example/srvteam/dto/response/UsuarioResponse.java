package com.example.srvteam.dto.response;

import java.time.LocalDateTime;

import com.example.srvteam.common.EnumTpAcesso;

public class UsuarioResponse {

    private Integer cdUsuario;
    private String login;
    private String nome;
    private String email;
    private LocalDateTime dataCadastro;
    private Boolean flAtivo;
    private LocalDateTime dtExpiracao;
    private Integer cdTpAcesso;
    private String tipoAcesso;

    // Construtores
    public UsuarioResponse() {}

    public UsuarioResponse(Integer cdUsuario, String login, String nome, String email, 
                          LocalDateTime dataCadastro, Boolean flAtivo, LocalDateTime dtExpiracao, 
                          Integer cdTpAcesso) {
        this.cdUsuario = cdUsuario;
        this.login = login;
        this.nome = nome;
        this.email = email;
        this.dataCadastro = dataCadastro;
        this.flAtivo = flAtivo;
        this.dtExpiracao = dtExpiracao;
        this.cdTpAcesso = cdTpAcesso;
        
        // Definir descrição do tipo de acesso
        if (cdTpAcesso != null) {
            for (EnumTpAcesso tipo : EnumTpAcesso.values()) {
                if (tipo.getCodigo() == cdTpAcesso) {
                    this.tipoAcesso = tipo.getDescricao();
                    break;
                }
            }
        }
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
        
        // Atualizar descrição do tipo de acesso
        if (cdTpAcesso != null) {
            for (EnumTpAcesso tipo : EnumTpAcesso.values()) {
                if (tipo.getCodigo() == cdTpAcesso) {
                    this.tipoAcesso = tipo.getDescricao();
                    break;
                }
            }
        }
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }
}
