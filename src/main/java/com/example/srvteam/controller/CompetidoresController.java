package com.example.srvteam.controller;

import com.example.srvteam.dto.request.CompetidorRequest;
import com.example.srvteam.dto.response.CompetidorResponse;
import com.example.srvteam.service.CompetidoresService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/competicao/competidor")
@PreAuthorize("isAuthenticated()")
public class CompetidoresController {
    @Autowired
    private CompetidoresService competidoresService;

    @PostMapping
    public ResponseEntity<CompetidorResponse> insCompetidor(@Valid @RequestBody CompetidorRequest request) {
        return ResponseEntity.ok(competidoresService.insCompetidor(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> delCompetidor(@RequestParam Integer cdCompetidor, @RequestParam Integer cdCompeticao) {
        competidoresService.delCompetidor(cdCompetidor, cdCompeticao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/por-competicao/{cdCompeticao}")
    public ResponseEntity<List<CompetidorResponse>> listCompetidorByCdCompeticao(@PathVariable Integer cdCompeticao) {
        return ResponseEntity.ok(competidoresService.listCompetidorByCdCompeticao(cdCompeticao));
    }
}
