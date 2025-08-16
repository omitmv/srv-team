package com.example.srvteam.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.example.srvteam.dto.request.CompeticaoBuscarRequest;
import com.example.srvteam.dto.request.CompeticaoRequest;
import com.example.srvteam.dto.response.CompeticaoResponse;
import com.example.srvteam.service.CompeticaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/competicao")
@PreAuthorize("isAuthenticated()")
public class CompeticaoController {

    private static final Logger logger = LoggerFactory.getLogger(CompeticaoController.class);

    @Autowired
    private CompeticaoService competicaoService;

    @PostMapping
    public ResponseEntity<CompeticaoResponse> insCompeticao(@Valid @RequestBody CompeticaoRequest request) {
        return ResponseEntity.ok(competicaoService.insCompeticao(request));
    }

    @PutMapping("/{cdCompeticao}")
    public ResponseEntity<CompeticaoResponse> updCompeticao(@PathVariable Integer cdCompeticao, @Valid @RequestBody CompeticaoRequest request) {
        return ResponseEntity.ok(competicaoService.updCompeticao(cdCompeticao, request));
    }

    @DeleteMapping("/{cdCompeticao}")
    public ResponseEntity<Void> delCompeticao(@PathVariable Integer cdCompeticao) {
        competicaoService.delCompeticao(cdCompeticao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CompeticaoResponse>> listCompeticaoByNmCompeticao(@RequestBody CompeticaoBuscarRequest competicaoBuscarRequest) {
        return ResponseEntity.ok(competicaoService.listCompeticaoByNmCompeticao(competicaoBuscarRequest.getNome()));
    }
}
