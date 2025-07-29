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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.SistemaRequest;
import com.example.srvteam.dto.SistemaResponse;
import com.example.srvteam.model.Sistema;
import com.example.srvteam.service.SistemaService;

import jakarta.validation.Valid;

/**
 * Controller para Sistema
 * Endpoints para operações CRUD de Sistema
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/sistema")
@CrossOrigin(origins = "*")
public class SistemaController {

    private static final Logger logger = LoggerFactory.getLogger(SistemaController.class);

    @Autowired
    private SistemaService sistemaService;

    /**
     * Converter Sistema para SistemaResponse
     */
    private SistemaResponse toResponse(Sistema sistema) {
        return new SistemaResponse(
                sistema.getCdSistema(),
                sistema.getDsSistema(),
                sistema.getFlAtivo()
        );
    }

    /**
     * Converter SistemaRequest para Sistema
     */
    private Sistema fromRequest(SistemaRequest request) {
        Sistema sistema = new Sistema();
        sistema.setDsSistema(request.getDsSistema());
        sistema.setFlAtivo(request.getFlAtivo());
        return sistema;
    }

    /**
     * Converter lista de Sistema para lista de SistemaResponse
     */
    private List<SistemaResponse> toResponseList(List<Sistema> sistemas) {
        return sistemas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/sistema - Inserir novo sistema
     * 
     * @param sistemaRequest Dados do sistema a ser criado
     * @return ResponseEntity<SistemaResponse>
     */
    @PostMapping
    public ResponseEntity<?> insSistema(@Valid @RequestBody SistemaRequest sistemaRequest) {
        try {
            logger.info("Request para inserir novo sistema: {}", sistemaRequest.getDsSistema());
            
            Sistema sistema = fromRequest(sistemaRequest);
            Sistema novoSistema = sistemaService.insSistema(sistema);
            
            logger.info("Sistema criado com sucesso. ID: {}", novoSistema.getCdSistema());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoSistema));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir sistema: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir sistema", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/sistema/{cdSistema} - Atualizar sistema existente
     * 
     * @param cdSistema Código do sistema a ser atualizado
     * @param sistemaRequest Dados atualizados do sistema
     * @return ResponseEntity<SistemaResponse>
     */
    @PutMapping("/{cdSistema}")
    public ResponseEntity<?> updSistema(@PathVariable Integer cdSistema, 
                                       @Valid @RequestBody SistemaRequest sistemaRequest) {
        try {
            logger.info("Request para atualizar sistema ID: {}", cdSistema);
            
            Sistema sistema = fromRequest(sistemaRequest);
            Sistema sistemaAtualizado = sistemaService.updSistema(cdSistema, sistema);
            
            logger.info("Sistema atualizado com sucesso. ID: {}", cdSistema);
            return ResponseEntity.ok(toResponse(sistemaAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar sistema ID {}: {}", cdSistema, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar sistema ID: {}", cdSistema, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/sistema/{cdSistema} - Deletar sistema (soft delete)
     * 
     * @param cdSistema Código do sistema a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdSistema}")
    public ResponseEntity<?> delSistema(@PathVariable Integer cdSistema) {
        try {
            logger.info("Request para deletar sistema ID: {}", cdSistema);
            
            boolean deletado = sistemaService.delSistema(cdSistema);
            
            if (deletado) {
                logger.info("Sistema deletado com sucesso. ID: {}", cdSistema);
                return ResponseEntity.ok("Sistema deletado com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao deletar sistema");
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar sistema ID {}: {}", cdSistema, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar sistema ID: {}", cdSistema, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/sistema/{cdSistema} - Obter sistema pelo código
     * 
     * @param cdSistema Código do sistema
     * @return ResponseEntity<SistemaResponse>
     */
    @GetMapping("/{cdSistema}")
    public ResponseEntity<?> getSistema(@PathVariable Integer cdSistema) {
        try {
            logger.debug("Request para buscar sistema ID: {}", cdSistema);
            
            Optional<Sistema> sistema = sistemaService.getSistema(cdSistema);
            
            if (sistema.isPresent()) {
                logger.debug("Sistema encontrado. ID: {}", cdSistema);
                return ResponseEntity.ok(toResponse(sistema.get()));
            } else {
                logger.warn("Sistema não encontrado. ID: {}", cdSistema);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Sistema não encontrado com o código: " + cdSistema);
            }
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar sistema ID: {}", cdSistema, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/sistema - Listar todos os sistemas ativos
     * 
     * @return ResponseEntity<List<SistemaResponse>>
     */
    @GetMapping
    public ResponseEntity<?> listSistema() {
        try {
            logger.debug("Request para listar sistemas ativos");
            
            List<Sistema> sistemas = sistemaService.listSistema();
            
            logger.debug("Retornando {} sistemas ativos", sistemas.size());
            return ResponseEntity.ok(toResponseList(sistemas));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar sistemas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/sistema/todos - Listar todos os sistemas (ativos e inativos)
     * Endpoint adicional para administração
     * 
     * @return ResponseEntity<List<SistemaResponse>>
     */
    @GetMapping("/todos")
    public ResponseEntity<?> listTodosSistemas() {
        try {
            logger.debug("Request para listar todos os sistemas");
            
            List<Sistema> sistemas = sistemaService.listTodosSistemas();
            
            logger.debug("Retornando {} sistemas no total", sistemas.size());
            return ResponseEntity.ok(toResponseList(sistemas));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar todos os sistemas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PATCH /v1/sistema/{cdSistema}/ativar - Ativar sistema
     * 
     * @param cdSistema Código do sistema
     * @return ResponseEntity<SistemaResponse>
     */
    @PatchMapping("/{cdSistema}/ativar")
    public ResponseEntity<?> ativarSistema(@PathVariable Integer cdSistema) {
        try {
            logger.info("Request para ativar sistema ID: {}", cdSistema);
            
            Optional<Sistema> sistemaOpt = sistemaService.getSistema(cdSistema);
            if (sistemaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Sistema não encontrado com o código: " + cdSistema);
            }
            
            Sistema sistema = sistemaOpt.get();
            sistema.setFlAtivo(true);
            Sistema sistemaAtualizado = sistemaService.updSistema(cdSistema, sistema);
            
            logger.info("Sistema ativado com sucesso. ID: {}", cdSistema);
            return ResponseEntity.ok(toResponse(sistemaAtualizado));
            
        } catch (Exception e) {
            logger.error("Erro interno ao ativar sistema ID: {}", cdSistema, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PATCH /v1/sistema/{cdSistema}/inativar - Inativar sistema
     * 
     * @param cdSistema Código do sistema
     * @return ResponseEntity<SistemaResponse>
     */
    @PatchMapping("/{cdSistema}/inativar")
    public ResponseEntity<?> inativarSistema(@PathVariable Integer cdSistema) {
        try {
            logger.info("Request para inativar sistema ID: {}", cdSistema);
            
            boolean inativado = sistemaService.delSistema(cdSistema);
            
            if (inativado) {
                Optional<Sistema> sistema = sistemaService.getSistema(cdSistema);
                logger.info("Sistema inativado com sucesso. ID: {}", cdSistema);
                return ResponseEntity.ok(toResponse(sistema.get()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao inativar sistema");
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao inativar sistema ID {}: {}", cdSistema, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inativar sistema ID: {}", cdSistema, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
