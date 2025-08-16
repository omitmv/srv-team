package com.example.srvteam.dto.request;

import java.util.ArrayList;

public class AutomacaoRequest {

  private String action;
  private ArrayList<String> data;

  public AutomacaoRequest() { }

  public AutomacaoRequest(String action, ArrayList<String> data) {
    this.action = action;
    this.data = data;
  }

  // Getters e Setters
  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public ArrayList<String> getData() {
    return data;
  }

  public void setData(ArrayList<String> data) {
    this.data = data;
  }

}
