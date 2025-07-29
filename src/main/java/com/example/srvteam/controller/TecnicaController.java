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

import com.example.srvteam.dto.request.TecnicaRequest;
import com.example.srvteam.dto.response.TecnicaResponse;
import com.example.srvteam.model.Tecnica;
import com.example.srvteam.service.TecnicaService;

import jakarta.validation.Valid;

/**
 * Controller para Tecnica
 * Endpoints para operações CRUD de Tecnica
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/tecnica")
@CrossOrigin(origins = "*")
public class TecnicaController {

    private static final Logger logger = LoggerFactory.getLogger(TecnicaController.class);

    @Autowired
    private TecnicaService tecnicaService;

    /**
     * Converter Tecnica para TecnicaResponse
     */
    private TecnicaResponse toResponse(Tecnica tecnica) {
        return new TecnicaResponse(
                tecnica.getCdTecnica(),
                tecnica.getDsTecnica(),
                tecnica.getObs()
        );
    }

    /**
     * Converter TecnicaRequest para Tecnica
     */
    private Tecnica fromRequest(TecnicaRequest request) {
        Tecnica tecnica = new Tecnica();
        tecnica.setDsTecnica(request.getDsTecnica());
        tecnica.setObs(request.getObs());
        return tecnica;
    }

    /**
     * Converter lista de Tecnica para lista de TecnicaResponse
     */
    private List<TecnicaResponse> toResponseList(List<Tecnica> tecnicas) {
        return tecnicas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/tecnica - Inserir nova técnica
     * 
     * @param tecnicaRequest Dados da técnica a ser criada
     * @return ResponseEntity<TecnicaResponse>
     */
    @PostMapping
    public ResponseEntity<?> insTecnica(@Valid @RequestBody TecnicaRequest tecnicaRequest) {
        try {
            logger.info("Request para inserir nova técnica: {}", tecnicaRequest.getDsTecnica());
            
            Tecnica tecnica = fromRequest(tecnicaRequest);
            Tecnica novaTecnica = tecnicaService.insTecnica(tecnica);
            
            logger.info("Técnica criada com sucesso. ID: {}", novaTecnica.getCdTecnica());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novaTecnica));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir técnica: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir técnica", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/tecnica/{cdTecnica} - Atualizar técnica existente
     * 
     * @param cdTecnica Código da técnica a ser atualizada
     * @param tecnicaRequest Dados atualizados da técnica
     * @return ResponseEntity<TecnicaResponse>
     */
    @PutMapping("/{cdTecnica}")
    public ResponseEntity<?> updTecnica(@PathVariable Integer cdTecnica, 
                                       @Valid @RequestBody TecnicaRequest tecnicaRequest) {
        try {
            logger.info("Request para atualizar técnica ID: {}", cdTecnica);
            
            Tecnica tecnica = fromRequest(tecnicaRequest);
            tecnica.setCdTecnica(cdTecnica);
            Tecnica tecnicaAtualizada = tecnicaService.updTecnica(tecnica);
            
            logger.info("Técnica atualizada com sucesso. ID: {}", cdTecnica);
            return ResponseEntity.ok(toResponse(tecnicaAtualizada));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar técnica: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar técnica", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/tecnica/{cdTecnica} - Deletar técnica
     * 
     * @param cdTecnica Código da técnica a ser deletada
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdTecnica}")
    public ResponseEntity<?> delTecnica(@PathVariable Integer cdTecnica) {
        try {
            logger.info("Request para deletar técnica ID: {}", cdTecnica);
            
            tecnicaService.delTecnica(cdTecnica);
            
            logger.info("Técnica deletada com sucesso. ID: {}", cdTecnica);
            return ResponseEntity.ok("Técnica deletada com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar técnica: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar técnica", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/tecnica/{cdTecnica} - Obter técnica por código
     * 
     * @param cdTecnica Código da técnica
     * @return ResponseEntity<TecnicaResponse>
     */
    @GetMapping("/{cdTecnica}")
    public ResponseEntity<?> getTecnica(@PathVariable Integer cdTecnica) {
        try {
            logger.info("Request para buscar técnica ID: {}", cdTecnica);
            
            Tecnica tecnica = tecnicaService.getTecnica(cdTecnica);
            
            logger.info("Técnica encontrada: {}", tecnica.getDsTecnica());
            return ResponseEntity.ok(toResponse(tecnica));
            
        } catch (IllegalArgumentException e) {
            logger.error("Técnica não encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar técnica", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/tecnica - Listar todas as técnicas
     * 
     * @return ResponseEntity<List<TecnicaResponse>>
     */
    @GetMapping
    public ResponseEntity<?> listTecnica() {
        try {
            logger.info("Request para listar todas as técnicas");
            
            List<Tecnica> tecnicas = tecnicaService.listTecnica();
            
            logger.info("Encontradas {} técnicas", tecnicas.size());
            return ResponseEntity.ok(toResponseList(tecnicas));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar técnicas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
