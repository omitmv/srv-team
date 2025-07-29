package com.example.srvteam.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

  @NotBlank(message = "Login é obrigatório")
  private String login;

  @NotBlank(message = "Senha é obrigatória")
  private String senha;

  // Constructors
  public LoginRequest() {
  }

  public LoginRequest(String login, String senha) {
    this.login = login;
    this.senha = senha;
  }

  // Getters and Setters
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
}
