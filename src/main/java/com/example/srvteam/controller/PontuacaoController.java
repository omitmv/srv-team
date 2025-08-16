package com.example.srvteam.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.PontuacaoRequest;
import com.example.srvteam.dto.response.PontuacaoResponse;
import com.example.srvteam.service.PontuacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/pontuacao")
@PreAuthorize("isAuthenticated()")
public class PontuacaoController {
    @Autowired
    private PontuacaoService pontuacaoService;

    @PostMapping
    public ResponseEntity<PontuacaoResponse> insPontuacao(@Valid @RequestBody PontuacaoRequest request) {
        return ResponseEntity.ok(pontuacaoService.insPontuacao(request));
    }

    @PutMapping("/{cdPontuacao}")
    public ResponseEntity<PontuacaoResponse> updPontuacao(@PathVariable Integer cdPontuacao, @Valid @RequestBody PontuacaoRequest request) {
        return ResponseEntity.ok(pontuacaoService.updPontuacao(cdPontuacao, request));
    }

    @DeleteMapping("/{cdPontuacao}")
    public ResponseEntity<Void> delPontuacao(@PathVariable Integer cdPontuacao) {
        pontuacaoService.delPontuacao(cdPontuacao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-competicao/{cdCompeticao}")
    public ResponseEntity<List<PontuacaoResponse>> listPontuacaoByCdCompeticao(@PathVariable Integer cdCompeticao) {
        return ResponseEntity.ok(pontuacaoService.listPontuacaoByCdCompeticao(cdCompeticao));
    }
}
