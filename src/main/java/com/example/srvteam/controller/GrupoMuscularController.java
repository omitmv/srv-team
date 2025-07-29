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

import com.example.srvteam.dto.request.GrupoMuscularRequest;
import com.example.srvteam.dto.response.GrupoMuscularResponse;
import com.example.srvteam.model.GrupoMuscular;
import com.example.srvteam.service.GrupoMuscularService;

import jakarta.validation.Valid;

/**
 * Controller para GrupoMuscular
 * Endpoints para operações CRUD de GrupoMuscular
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/grupo-muscular")
@CrossOrigin(origins = "*")
public class GrupoMuscularController {

    private static final Logger logger = LoggerFactory.getLogger(GrupoMuscularController.class);

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    /**
     * Converter GrupoMuscular para GrupoMuscularResponse
     */
    private GrupoMuscularResponse toResponse(GrupoMuscular grupoMuscular) {
        return new GrupoMuscularResponse(
                grupoMuscular.getCdGrupoMuscular(),
                grupoMuscular.getDsGrupoMuscular()
        );
    }

    /**
     * Converter GrupoMuscularRequest para GrupoMuscular
     */
    private GrupoMuscular fromRequest(GrupoMuscularRequest request) {
        GrupoMuscular grupoMuscular = new GrupoMuscular();
        grupoMuscular.setDsGrupoMuscular(request.getDsGrupoMuscular());
        return grupoMuscular;
    }

    /**
     * Converter lista de GrupoMuscular para lista de GrupoMuscularResponse
     */
    private List<GrupoMuscularResponse> toResponseList(List<GrupoMuscular> gruposMusculares) {
        return gruposMusculares.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/grupo-muscular - Inserir novo grupo muscular
     * 
     * @param grupoMuscularRequest Dados do grupo muscular a ser criado
     * @return ResponseEntity<GrupoMuscularResponse>
     */
    @PostMapping
    public ResponseEntity<?> insGrupoMuscular(@Valid @RequestBody GrupoMuscularRequest grupoMuscularRequest) {
        try {
            logger.info("Request para inserir novo grupo muscular: {}", grupoMuscularRequest.getDsGrupoMuscular());
            
            GrupoMuscular grupoMuscular = fromRequest(grupoMuscularRequest);
            GrupoMuscular novoGrupoMuscular = grupoMuscularService.insGrupoMuscular(grupoMuscular);
            
            logger.info("Grupo muscular criado com sucesso. ID: {}", novoGrupoMuscular.getCdGrupoMuscular());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoGrupoMuscular));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir grupo muscular: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir grupo muscular", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/grupo-muscular/{cdGrupoMuscular} - Atualizar grupo muscular existente
     * 
     * @param cdGrupoMuscular Código do grupo muscular a ser atualizado
     * @param grupoMuscularRequest Dados atualizados do grupo muscular
     * @return ResponseEntity<GrupoMuscularResponse>
     */
    @PutMapping("/{cdGrupoMuscular}")
    public ResponseEntity<?> updGrupoMuscular(@PathVariable Integer cdGrupoMuscular, 
                                             @Valid @RequestBody GrupoMuscularRequest grupoMuscularRequest) {
        try {
            logger.info("Request para atualizar grupo muscular ID: {}", cdGrupoMuscular);
            
            GrupoMuscular grupoMuscular = fromRequest(grupoMuscularRequest);
            grupoMuscular.setCdGrupoMuscular(cdGrupoMuscular);
            GrupoMuscular grupoMuscularAtualizado = grupoMuscularService.updGrupoMuscular(grupoMuscular);
            
            logger.info("Grupo muscular atualizado com sucesso. ID: {}", cdGrupoMuscular);
            return ResponseEntity.ok(toResponse(grupoMuscularAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar grupo muscular: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar grupo muscular", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/grupo-muscular/{cdGrupoMuscular} - Deletar grupo muscular
     * 
     * @param cdGrupoMuscular Código do grupo muscular a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdGrupoMuscular}")
    public ResponseEntity<?> delGrupoMuscular(@PathVariable Integer cdGrupoMuscular) {
        try {
            logger.info("Request para deletar grupo muscular ID: {}", cdGrupoMuscular);
            
            grupoMuscularService.delGrupoMuscular(cdGrupoMuscular);
            
            logger.info("Grupo muscular deletado com sucesso. ID: {}", cdGrupoMuscular);
            return ResponseEntity.ok("Grupo muscular deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar grupo muscular: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar grupo muscular", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/grupo-muscular/{cdGrupoMuscular} - Obter grupo muscular por código
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return ResponseEntity<GrupoMuscularResponse>
     */
    @GetMapping("/{cdGrupoMuscular}")
    public ResponseEntity<?> getGrupoMuscular(@PathVariable Integer cdGrupoMuscular) {
        try {
            logger.info("Request para buscar grupo muscular ID: {}", cdGrupoMuscular);
            
            GrupoMuscular grupoMuscular = grupoMuscularService.getGrupoMuscular(cdGrupoMuscular);
            
            logger.info("Grupo muscular encontrado: {}", grupoMuscular.getDsGrupoMuscular());
            return ResponseEntity.ok(toResponse(grupoMuscular));
            
        } catch (IllegalArgumentException e) {
            logger.error("Grupo muscular não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar grupo muscular", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/grupo-muscular - Listar todos os grupos musculares
     * 
     * @return ResponseEntity<List<GrupoMuscularResponse>>
     */
    @GetMapping
    public ResponseEntity<?> listGrupoMuscular() {
        try {
            logger.info("Request para listar todos os grupos musculares");
            
            List<GrupoMuscular> gruposMusculares = grupoMuscularService.listGrupoMuscular();
            
            logger.info("Encontrados {} grupos musculares", gruposMusculares.size());
            return ResponseEntity.ok(toResponseList(gruposMusculares));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar grupos musculares", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
