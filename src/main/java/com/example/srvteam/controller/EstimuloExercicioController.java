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

import com.example.srvteam.dto.request.EstimuloExercicioRequest;
import com.example.srvteam.dto.response.EstimuloExercicioResponse;
import com.example.srvteam.model.EstimuloExercicio;
import com.example.srvteam.service.EstimuloExercicioService;

import jakarta.validation.Valid;

/**
 * Controller para EstimuloExercicio
 * Endpoints para operações CRUD de EstimuloExercicio
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/estimulo-exercicio")
@CrossOrigin(origins = "*")
public class EstimuloExercicioController {

    private static final Logger logger = LoggerFactory.getLogger(EstimuloExercicioController.class);

    @Autowired
    private EstimuloExercicioService estimuloExercicioService;

    /**
     * Converter EstimuloExercicio para EstimuloExercicioResponse
     */
    private EstimuloExercicioResponse toResponse(EstimuloExercicio estimuloExercicio) {
        String dsEstimulo = null;
        String dsExercicio = null;
        String dsTecnica = null;

        if (estimuloExercicio.getEstimulo() != null) {
            dsEstimulo = estimuloExercicio.getEstimulo().getDsEstimulo();
        }

        if (estimuloExercicio.getExercicio() != null) {
            dsExercicio = estimuloExercicio.getExercicio().getDsExercicio();
        }

        if (estimuloExercicio.getTecnica() != null) {
            dsTecnica = estimuloExercicio.getTecnica().getDsTecnica();
        }

        return new EstimuloExercicioResponse(
                estimuloExercicio.getCdEstimulo(),
                dsEstimulo,
                estimuloExercicio.getCdExercicio(),
                dsExercicio,
                estimuloExercicio.getSerie(),
                estimuloExercicio.getIntervalo(),
                estimuloExercicio.getCdTecnica(),
                dsTecnica,
                estimuloExercicio.getObs()
        );
    }

    /**
     * Converter EstimuloExercicioRequest para EstimuloExercicio
     */
    private EstimuloExercicio fromRequest(EstimuloExercicioRequest request) {
        EstimuloExercicio estimuloExercicio = new EstimuloExercicio();
        estimuloExercicio.setCdEstimulo(request.getCdEstimulo());
        estimuloExercicio.setCdExercicio(request.getCdExercicio());
        estimuloExercicio.setSerie(request.getSerie());
        estimuloExercicio.setIntervalo(request.getIntervalo());
        estimuloExercicio.setCdTecnica(request.getCdTecnica());
        estimuloExercicio.setObs(request.getObs());
        return estimuloExercicio;
    }

    /**
     * Converter lista de EstimuloExercicio para lista de EstimuloExercicioResponse
     */
    private List<EstimuloExercicioResponse> toResponseList(List<EstimuloExercicio> estimuloExercicios) {
        return estimuloExercicios.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/estimulo-exercicio - Inserir novo exercício em um estímulo
     * 
     * @param estimuloExercicioRequest Dados do EstimuloExercicio a ser criado
     * @return ResponseEntity<EstimuloExercicioResponse>
     */
    @PostMapping
    public ResponseEntity<?> insEstimuloExercicio(@Valid @RequestBody EstimuloExercicioRequest estimuloExercicioRequest) {
        try {
            logger.info("Request para inserir EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                       estimuloExercicioRequest.getCdEstimulo(), estimuloExercicioRequest.getCdExercicio());
            
            EstimuloExercicio estimuloExercicio = fromRequest(estimuloExercicioRequest);
            EstimuloExercicio novoEstimuloExercicio = estimuloExercicioService.insEstimuloExercicio(estimuloExercicio);
            
            logger.info("EstimuloExercicio criado com sucesso: cdEstimulo={}, cdExercicio={}", 
                       novoEstimuloExercicio.getCdEstimulo(), novoEstimuloExercicio.getCdExercicio());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoEstimuloExercicio));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir EstimuloExercicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir EstimuloExercicio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/estimulo-exercicio/{cdEstimulo}/{cdExercicio} - Atualizar EstimuloExercicio existente
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @param estimuloExercicioRequest Dados atualizados do EstimuloExercicio
     * @return ResponseEntity<EstimuloExercicioResponse>
     */
    @PutMapping("/{cdEstimulo}/{cdExercicio}")
    public ResponseEntity<?> updEstimuloExercicio(@PathVariable Integer cdEstimulo,
                                                 @PathVariable Integer cdExercicio,
                                                 @Valid @RequestBody EstimuloExercicioRequest estimuloExercicioRequest) {
        try {
            logger.info("Request para atualizar EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            
            EstimuloExercicio estimuloExercicio = fromRequest(estimuloExercicioRequest);
            estimuloExercicio.setCdEstimulo(cdEstimulo);
            estimuloExercicio.setCdExercicio(cdExercicio);
            
            EstimuloExercicio estimuloExercicioAtualizado = estimuloExercicioService.updEstimuloExercicio(estimuloExercicio);
            
            logger.info("EstimuloExercicio atualizado com sucesso: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            return ResponseEntity.ok(toResponse(estimuloExercicioAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar EstimuloExercicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar EstimuloExercicio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/estimulo-exercicio/{cdEstimulo}/{cdExercicio} - Deletar EstimuloExercicio
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdEstimulo}/{cdExercicio}")
    public ResponseEntity<?> delEstimuloExercicio(@PathVariable Integer cdEstimulo,
                                                 @PathVariable Integer cdExercicio) {
        try {
            logger.info("Request para deletar EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            
            estimuloExercicioService.delEstimuloExercicio(cdEstimulo, cdExercicio);
            
            logger.info("EstimuloExercicio deletado com sucesso: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            return ResponseEntity.ok("EstimuloExercicio deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar EstimuloExercicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar EstimuloExercicio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/estimulo-exercicio/{cdEstimulo}/{cdExercicio} - Obter EstimuloExercicio por códigos
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @return ResponseEntity<EstimuloExercicioResponse>
     */
    @GetMapping("/{cdEstimulo}/{cdExercicio}")
    public ResponseEntity<?> getEstimuloExercicio(@PathVariable Integer cdEstimulo,
                                                 @PathVariable Integer cdExercicio) {
        try {
            logger.info("Request para buscar EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            
            EstimuloExercicio estimuloExercicio = estimuloExercicioService.getEstimuloExercicio(cdEstimulo, cdExercicio);
            
            logger.info("EstimuloExercicio encontrado: cdEstimulo={}, cdExercicio={}", 
                       cdEstimulo, cdExercicio);
            return ResponseEntity.ok(toResponse(estimuloExercicio));
            
        } catch (IllegalArgumentException e) {
            logger.error("EstimuloExercicio não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar EstimuloExercicio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/estimulo-exercicio/by-estimulo/{cdEstimulo} - Listar exercícios de um estímulo
     * 
     * @param cdEstimulo Código do estímulo
     * @return ResponseEntity<List<EstimuloExercicioResponse>>
     */
    @GetMapping("/by-estimulo/{cdEstimulo}")
    public ResponseEntity<?> listEstimuloExercicioByEstimulo(@PathVariable Integer cdEstimulo) {
        try {
            logger.info("Request para listar EstimuloExercicio do estímulo ID: {}", cdEstimulo);
            
            List<EstimuloExercicio> estimuloExercicios = estimuloExercicioService
                    .listEstimuloExercicioByEstimulo(cdEstimulo);
            
            logger.info("Encontrados {} EstimuloExercicio para estímulo ID: {}", 
                       estimuloExercicios.size(), cdEstimulo);
            return ResponseEntity.ok(toResponseList(estimuloExercicios));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao listar EstimuloExercicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar EstimuloExercicio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
