package com.example.srvteam.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.common.EnumTpAcesso;
import com.example.srvteam.dto.response.TipoAcessoResponse;

@RestController
@RequestMapping("/v1/tipo-acesso")
@CrossOrigin(origins = "*")
public class TipoAcessoController {

    /**
     * GET /v1/tipo-acesso - Listar todos os tipos de acesso disponíveis
     */
    @GetMapping
    public ResponseEntity<List<TipoAcessoResponse>> getAllTiposAcesso() {
        List<TipoAcessoResponse> tiposAcesso = Arrays.stream(EnumTpAcesso.values())
                .map(tipo -> new TipoAcessoResponse(tipo.getCodigo(), tipo.getDescricao()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(tiposAcesso);
    }
}
