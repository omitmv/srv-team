package com.example.srvteam.dto.response;

import com.example.srvteam.common.EnumTpAcesso;

public class LoginResponse {

  private String token;
  private Integer cdUsuario;
  private String login;
  private String nome;
  private String email;
  private Long expiresIn;
  private Integer cdTpAcesso;
  private String tipoAcesso;

  // Constructors
  public LoginResponse() {
  }

  public LoginResponse(String token, Integer cdUsuario, String login, String nome, String email, Long expiresIn) {
    this.token = token;
    this.cdUsuario = cdUsuario;
    this.login = login;
    this.nome = nome;
    this.email = email;
    this.expiresIn = expiresIn;
  }
  
  public LoginResponse(String token, Integer cdUsuario, String login, String nome, String email, Long expiresIn, Integer cdTpAcesso) {
    this.token = token;
    this.cdUsuario = cdUsuario;
    this.login = login;
    this.nome = nome;
    this.email = email;
    this.expiresIn = expiresIn;
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

  // Getters and Setters
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

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

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
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
