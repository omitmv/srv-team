package com.example.srvteam.dto;

public class LoginResponse {

  private String token;
  private String tipo = "Bearer";
  private Integer cdUsuario;
  private String login;
  private String nome;
  private String email;
  private Long expiresIn; // Em milissegundos

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

  // Getters and Setters
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
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
}
