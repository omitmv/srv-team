package com.example.srvteam.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.TreinoEstimuloRequest;
import com.example.srvteam.dto.response.TreinoEstimuloResponse;
import com.example.srvteam.model.TreinoEstimulo;
import com.example.srvteam.service.TreinoEstimuloService;

import jakarta.validation.Valid;

/**
 * Controller para TreinoEstimulo
 * Endpoints para operações CRUD de TreinoEstimulo
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/treino-estimulo")
@CrossOrigin(origins = "*")
public class TreinoEstimuloController {

    private static final Logger logger = LoggerFactory.getLogger(TreinoEstimuloController.class);

    @Autowired
    private TreinoEstimuloService treinoEstimuloService;

    /**
     * Converter TreinoEstimulo para TreinoEstimuloResponse
     */
    private TreinoEstimuloResponse toResponse(TreinoEstimulo treinoEstimulo) {
        String dsTreino = null;
        String dsEstimulo = null;
        
        if (treinoEstimulo.getTreino() != null) {
            dsTreino = treinoEstimulo.getTreino().getDsTreino();
        }
        
        if (treinoEstimulo.getEstimulo() != null) {
            dsEstimulo = treinoEstimulo.getEstimulo().getDsEstimulo();
        }
        
        return new TreinoEstimuloResponse(
                treinoEstimulo.getCdTreino(),
                dsTreino,
                treinoEstimulo.getCdEstimulo(),
                dsEstimulo
        );
    }

    /**
     * Converter TreinoEstimuloRequest para TreinoEstimulo
     */
    private TreinoEstimulo fromRequest(TreinoEstimuloRequest request) {
        TreinoEstimulo treinoEstimulo = new TreinoEstimulo();
        treinoEstimulo.setCdTreino(request.getCdTreino());
        treinoEstimulo.setCdEstimulo(request.getCdEstimulo());
        return treinoEstimulo;
    }

    /**
     * Converter lista de TreinoEstimulo para lista de TreinoEstimuloResponse
     */
    private List<TreinoEstimuloResponse> toResponseList(List<TreinoEstimulo> treinoEstimulos) {
        return treinoEstimulos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/treino-estimulo - Inserir novo treino com estímulo
     * 
     * @param treinoEstimuloRequest Dados da relação treino-estímulo a ser criada
     * @return ResponseEntity<TreinoEstimuloResponse>
     */
    @PostMapping
    public ResponseEntity<?> insTreinoEstimulo(@Valid @RequestBody TreinoEstimuloRequest treinoEstimuloRequest) {
        try {
            logger.info("Request para inserir nova relação treino-estímulo: treino={}, estimulo={}", 
                       treinoEstimuloRequest.getCdTreino(), treinoEstimuloRequest.getCdEstimulo());
            
            TreinoEstimulo treinoEstimulo = fromRequest(treinoEstimuloRequest);
            TreinoEstimulo novoTreinoEstimulo = treinoEstimuloService.insTreinoEstimulo(treinoEstimulo);
            
            logger.info("Relação treino-estímulo criada com sucesso: treino={}, estimulo={}", 
                       novoTreinoEstimulo.getCdTreino(), novoTreinoEstimulo.getCdEstimulo());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoTreinoEstimulo));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir relação treino-estímulo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro interno ao inserir relação treino-estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/treino-estimulo/{cdTreino}/{cdEstimulo} - Deletar relação treino-estímulo
     * 
     * @param cdTreino ID do treino
     * @param cdEstimulo ID do estímulo
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdTreino}/{cdEstimulo}")
    public ResponseEntity<?> delTreinoEstimulo(@PathVariable Integer cdTreino, 
                                              @PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para deletar relação treino-estímulo: treino={}, estimulo={}", 
                       cdTreino, cdEstimulo);
            
            treinoEstimuloService.delTreinoEstimulo(cdTreino, cdEstimulo);
            
            logger.info("Relação treino-estímulo deletada com sucesso: treino={}, estimulo={}", 
                       cdTreino, cdEstimulo);
            return ResponseEntity.ok("Relação treino-estímulo deletada com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar relação treino-estímulo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro interno ao deletar relação treino-estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino-estimulo/{cdTreino}/{cdEstimulo} - Obter relação treino-estímulo
     * 
     * @param cdTreino ID do treino
     * @param cdEstimulo ID do estímulo
     * @return ResponseEntity<TreinoEstimuloResponse>
     */
    @GetMapping("/{cdTreino}/{cdEstimulo}")
    public ResponseEntity<?> getTreinoEstimulo(@PathVariable Integer cdTreino, 
                                              @PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para buscar relação treino-estímulo: treino={}, estimulo={}", 
                       cdTreino, cdEstimulo);
            
            Optional<TreinoEstimulo> treinoEstimulo = treinoEstimuloService.getTreinoEstimulo(cdTreino, cdEstimulo);
            
            if (treinoEstimulo.isPresent()) {
                logger.info("Relação treino-estímulo encontrada: treino={}, estimulo={}", 
                           cdTreino, cdEstimulo);
                return ResponseEntity.ok(toResponse(treinoEstimulo.get()));
            } else {
                logger.warn("Relação treino-estímulo não encontrada: treino={}, estimulo={}", 
                           cdTreino, cdEstimulo);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar relação treino-estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino-estimulo/treino/{cdTreino} - Listar estímulos de um treino
     * 
     * @param cdTreino ID do treino
     * @return ResponseEntity<List<TreinoEstimuloResponse>>
     */
    @GetMapping("/treino/{cdTreino}")
    public ResponseEntity<?> listTreinoEstimuloByTreino(@PathVariable Integer cdTreino) {
        try {
            logger.info("Request para listar estímulos do treino: {}", cdTreino);
            
            List<TreinoEstimulo> treinoEstimulos = treinoEstimuloService.listTreinoEstimuloByTreino(cdTreino);
            
            logger.info("Encontrados {} estímulos para o treino: {}", treinoEstimulos.size(), cdTreino);
            return ResponseEntity.ok(toResponseList(treinoEstimulos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar estímulos por treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino-estimulo/estimulo/{cdEstimulo} - Listar treinos de um estímulo
     * 
     * @param cdEstimulo ID do estímulo
     * @return ResponseEntity<List<TreinoEstimuloResponse>>
     */
    @GetMapping("/estimulo/{cdEstimulo}")
    public ResponseEntity<?> listTreinoEstimuloByEstimulo(@PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para listar treinos do estímulo: {}", cdEstimulo);
            
            List<TreinoEstimulo> treinoEstimulos = treinoEstimuloService.listTreinoEstimuloByEstimulo(cdEstimulo);
            
            logger.info("Encontrados {} treinos para o estímulo: {}", treinoEstimulos.size(), cdEstimulo);
            return ResponseEntity.ok(toResponseList(treinoEstimulos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar treinos por estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
