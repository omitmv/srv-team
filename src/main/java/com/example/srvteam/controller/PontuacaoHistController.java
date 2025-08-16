package com.example.srvteam.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.PontuacaoHistPorPosicaoRequest;
import com.example.srvteam.dto.request.PontuacaoHistRequest;
import com.example.srvteam.dto.response.PontuacaoHistResponse;
import com.example.srvteam.service.PontuacaoHistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/pontuacao-hist")
@PreAuthorize("isAuthenticated()")
public class PontuacaoHistController {
    @Autowired
    private PontuacaoHistService pontuacaoHistService;

    @PostMapping
    public ResponseEntity<PontuacaoHistResponse> insPontuacaoHist(@Valid @RequestBody PontuacaoHistRequest request) {
        return ResponseEntity.ok(pontuacaoHistService.insPontuacaoHist(request));
    }

    @PutMapping("/{cdPontuacaoHist}")
    public ResponseEntity<PontuacaoHistResponse> updPontuacaoHist(@PathVariable Integer cdPontuacaoHist, @Valid @RequestBody PontuacaoHistRequest request) {
        return ResponseEntity.ok(pontuacaoHistService.updPontuacaoHist(cdPontuacaoHist, request));
    }

    @DeleteMapping("/{cdPontuacaoHist}")
    public ResponseEntity<Void> delPontuacaoHist(@PathVariable Integer cdPontuacaoHist) {
        pontuacaoHistService.delPontuacaoHist(cdPontuacaoHist);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-periodo")
    public ResponseEntity<List<PontuacaoHistResponse>> listPontuacaoHistByPeriodo(
            @RequestParam Integer cdCompetidor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(pontuacaoHistService.listPontuacaoHistByPeriodo(cdCompetidor, inicio, fim));
    }

    @GetMapping("/por-competidor-competicao")
    public ResponseEntity<List<PontuacaoHistResponse>> listPontuacaoHistByCdCompetidorInCdCompeticao(
            @RequestParam Integer cdCompetidor,
            @RequestParam Integer cdCompeticao) {
        return ResponseEntity.ok(pontuacaoHistService.listPontuacaoHistByCdCompetidorInCdCompeticao(cdCompetidor, cdCompeticao));
    }

    @GetMapping("/por-competicao/{cdCompeticao}")
    public ResponseEntity<List<PontuacaoHistResponse>> listPontuacaoHistByCdCompeticao(@PathVariable Integer cdCompeticao) {
        return ResponseEntity.ok(pontuacaoHistService.listPontuacaoHistByCdCompeticao(cdCompeticao));
    }

    /**
     * POST /v1/pontuacao-hist/por-posicao - Insere histórico de pontuação por posição
     * Body: { "cdCompetidor": 9, "cdCompeticao": 1, "posicao": 2 }
     */
    @PostMapping("/por-posicao")
    public ResponseEntity<?> insPontuacaoHistPorPosicao(@RequestBody PontuacaoHistPorPosicaoRequest req) {
        try {
            PontuacaoHistResponse response = pontuacaoHistService.insPontuacaoHistPorPosicao(req);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
