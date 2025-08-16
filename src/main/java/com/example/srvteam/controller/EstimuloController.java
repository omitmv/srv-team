package com.example.srvteam.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.EstimuloRequest;
import com.example.srvteam.dto.response.EstimuloResponse;
import com.example.srvteam.model.Estimulo;
import com.example.srvteam.service.EstimuloService;

import jakarta.validation.Valid;

/**
 * Controller para Estimulo
 * Endpoints para operações CRUD de Estimulo
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/estimulo")
@CrossOrigin(origins = "*")
public class EstimuloController {

    private static final Logger logger = LoggerFactory.getLogger(EstimuloController.class);

    @Autowired
    private EstimuloService estimuloService;

    /**
     * Converter Estimulo para EstimuloResponse
     */
    private EstimuloResponse toResponse(Estimulo estimulo) {
        return new EstimuloResponse(
                estimulo.getCdEstimulo(),
                estimulo.getDsEstimulo(),
                estimulo.getObs()
        );
    }

    /**
     * Converter EstimuloRequest para Estimulo
     */
    private Estimulo fromRequest(EstimuloRequest request) {
        Estimulo estimulo = new Estimulo();
        estimulo.setDsEstimulo(request.getDsEstimulo());
        estimulo.setObs(request.getObs());
        return estimulo;
    }

    /**
     * Converter lista de Estimulo para lista de EstimuloResponse
     */
    private List<EstimuloResponse> toResponseList(List<Estimulo> estimulos) {
        return estimulos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/estimulo - Inserir novo estímulo
     * 
     * @param estimuloRequest Dados do estímulo a ser criado
     * @return ResponseEntity<EstimuloResponse>
     */
    @PostMapping
    public ResponseEntity<?> insEstimulo(@Valid @RequestBody EstimuloRequest estimuloRequest) {
        try {
            logger.info("Request para inserir novo estímulo: {}", estimuloRequest.getDsEstimulo());
            
            Estimulo estimulo = fromRequest(estimuloRequest);
            Estimulo novoEstimulo = estimuloService.insEstimulo(estimulo);
            
            logger.info("Estímulo criado com sucesso. ID: {}", novoEstimulo.getCdEstimulo());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoEstimulo));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir estímulo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/estimulo/{cdEstimulo} - Atualizar estímulo existente
     * 
     * @param cdEstimulo Código do estímulo a ser atualizado
     * @param estimuloRequest Dados atualizados do estímulo
     * @return ResponseEntity<EstimuloResponse>
     */
    @PutMapping("/{cdEstimulo}")
    public ResponseEntity<?> updEstimulo(@PathVariable Integer cdEstimulo, 
                                        @Valid @RequestBody EstimuloRequest estimuloRequest) {
        try {
            logger.info("Request para atualizar estímulo ID: {}", cdEstimulo);
            
            Estimulo estimulo = fromRequest(estimuloRequest);
            estimulo.setCdEstimulo(cdEstimulo);
            Estimulo estimuloAtualizado = estimuloService.updEstimulo(estimulo);
            
            logger.info("Estímulo atualizado com sucesso. ID: {}", cdEstimulo);
            return ResponseEntity.ok(toResponse(estimuloAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar estímulo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/estimulo/{cdEstimulo} - Deletar estímulo
     * 
     * @param cdEstimulo Código do estímulo a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdEstimulo}")
    public ResponseEntity<?> delEstimulo(@PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para deletar estímulo ID: {}", cdEstimulo);
            
            estimuloService.delEstimulo(cdEstimulo);
            
            logger.info("Estímulo deletado com sucesso. ID: {}", cdEstimulo);
            return ResponseEntity.ok("Estímulo deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar estímulo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/estimulo/{cdEstimulo} - Obter estímulo por código
     * 
     * @param cdEstimulo Código do estímulo
     * @return ResponseEntity<EstimuloResponse>
     */
    @GetMapping("/{cdEstimulo}")
    public ResponseEntity<?> getEstimulo(@PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para buscar estímulo ID: {}", cdEstimulo);
            
            Estimulo estimulo = estimuloService.getEstimulo(cdEstimulo);
            
            logger.info("Estímulo encontrado: {}", estimulo.getDsEstimulo());
            return ResponseEntity.ok(toResponse(estimulo));
            
        } catch (IllegalArgumentException e) {
            logger.error("Estímulo não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar estímulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/estimulo - Listar todos os estímulos
     * 
     * @return ResponseEntity<List<EstimuloResponse>>
     */
    @GetMapping
    public ResponseEntity<?> listEstimulo() {
        try {
            logger.info("Request para listar todos os estímulos");
            
            List<Estimulo> estimulos = estimuloService.listEstimulo();
            
            logger.info("Encontrados {} estímulos", estimulos.size());
            return ResponseEntity.ok(toResponseList(estimulos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar estímulos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/estimulo/treino/{cdTreino} - Listar estímulos de um treino específico
     * 
     * @param cdTreino Código do treino
     * @return ResponseEntity<List<EstimuloResponse>>
     * 
     * @apiNote Este endpoint será implementado quando os relacionamentos 
     *          entre Estimulo e Treino forem estabelecidos.
     */
    @GetMapping("/treino/{cdTreino}")
    public ResponseEntity<?> listEstimuloByTreino(@PathVariable Integer cdTreino) {
        try {
            logger.info("Request para listar estímulos do treino ID: {}", cdTreino);
            
            List<Estimulo> estimulos = estimuloService.listEstimuloByTreino(cdTreino);
            
            logger.info("Encontrados {} estímulos para o treino ID: {}", estimulos.size(), cdTreino);
            return ResponseEntity.ok(toResponseList(estimulos));
            
        } catch (UnsupportedOperationException e) {
            logger.warn("Funcionalidade não implementada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Funcionalidade será implementada quando os relacionamentos com Treino forem estabelecidos");
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar estímulos por treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
