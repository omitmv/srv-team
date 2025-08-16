package com.example.srvteam.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.AutomacaoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("v1/automacao")
@CrossOrigin(origins = "*")
public class AutomacaoController {

  private static final Logger logger = LoggerFactory.getLogger(AutomacaoController.class);

  @PostMapping
  public ResponseEntity<?> startIteraction(@Valid @RequestBody AutomacaoRequest request) {
    try {
      logger.info("Iniciando interação com a automação: {}", request.getData());
      return ResponseEntity.status(HttpStatus.OK).body("Ok");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
  
}
