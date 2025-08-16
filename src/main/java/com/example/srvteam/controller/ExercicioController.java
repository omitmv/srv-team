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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.ExercicioRequest;
import com.example.srvteam.dto.response.ExercicioResponse;
import com.example.srvteam.model.Exercicio;
import com.example.srvteam.service.ExercicioService;

import jakarta.validation.Valid;

/**
 * Controller para Exercicio
 * Endpoints para operações CRUD de Exercicio
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/exercicio")
@CrossOrigin(origins = "*")
public class ExercicioController {

    private static final Logger logger = LoggerFactory.getLogger(ExercicioController.class);

    @Autowired
    private ExercicioService exercicioService;

    /**
     * Converter Exercicio para ExercicioResponse
     */
    private ExercicioResponse toResponse(Exercicio exercicio) {
        String dsGrupoMuscular = null;
        if (exercicio.getGrupoMuscular() != null) {
            dsGrupoMuscular = exercicio.getGrupoMuscular().getDsGrupoMuscular();
        }
        
        return new ExercicioResponse(
                exercicio.getCdExercicio(),
                exercicio.getDsExercicio(),
                exercicio.getCdGrupoMuscular(),
                dsGrupoMuscular,
                exercicio.getVideo()
        );
    }

    /**
     * Converter ExercicioRequest para Exercicio
     */
    private Exercicio fromRequest(ExercicioRequest request) {
        Exercicio exercicio = new Exercicio();
        exercicio.setDsExercicio(request.getDsExercicio());
        exercicio.setCdGrupoMuscular(request.getCdGrupoMuscular());
        exercicio.setVideo(request.getVideo());
        return exercicio;
    }

    /**
     * Converter lista de Exercicio para lista de ExercicioResponse
     */
    private List<ExercicioResponse> toResponseList(List<Exercicio> exercicios) {
        return exercicios.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/exercicio - Inserir novo exercício
     * 
     * @param exercicioRequest Dados do exercício a ser criado
     * @return ResponseEntity<ExercicioResponse>
     */
    @PostMapping
    public ResponseEntity<?> insExercicio(@Valid @RequestBody ExercicioRequest exercicioRequest) {
        try {
            logger.info("Request para inserir novo exercício: {}", exercicioRequest.getDsExercicio());
            
            Exercicio exercicio = fromRequest(exercicioRequest);
            Exercicio novoExercicio = exercicioService.insExercicio(exercicio);
            
            logger.info("Exercício criado com sucesso. ID: {}", novoExercicio.getCdExercicio());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoExercicio));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir exercício: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir exercício", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/exercicio/{cdExercicio} - Atualizar exercício existente
     * 
     * @param cdExercicio Código do exercício a ser atualizado
     * @param exercicioRequest Dados atualizados do exercício
     * @return ResponseEntity<ExercicioResponse>
     */
    @PutMapping("/{cdExercicio}")
    public ResponseEntity<?> updExercicio(@PathVariable Integer cdExercicio, 
                                         @Valid @RequestBody ExercicioRequest exercicioRequest) {
        try {
            logger.info("Request para atualizar exercício ID: {}", cdExercicio);
            
            Exercicio exercicio = fromRequest(exercicioRequest);
            exercicio.setCdExercicio(cdExercicio);
            Exercicio exercicioAtualizado = exercicioService.updExercicio(exercicio);
            
            logger.info("Exercício atualizado com sucesso. ID: {}", cdExercicio);
            return ResponseEntity.ok(toResponse(exercicioAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar exercício: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar exercício", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/exercicio/{cdExercicio} - Deletar exercício
     * 
     * @param cdExercicio Código do exercício a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdExercicio}")
    public ResponseEntity<?> delExercicio(@PathVariable Integer cdExercicio) {
        try {
            logger.info("Request para deletar exercício ID: {}", cdExercicio);
            
            exercicioService.delExercicio(cdExercicio);
            
            logger.info("Exercício deletado com sucesso. ID: {}", cdExercicio);
            return ResponseEntity.ok("Exercício deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar exercício: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar exercício", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/exercicio/{cdExercicio} - Obter exercício por código
     * 
     * @param cdExercicio Código do exercício
     * @return ResponseEntity<ExercicioResponse>
     */
    @GetMapping("/{cdExercicio}")
    public ResponseEntity<?> getExercicio(@PathVariable Integer cdExercicio) {
        try {
            logger.info("Request para buscar exercício ID: {}", cdExercicio);
            
            Exercicio exercicio = exercicioService.getExercicio(cdExercicio);
            
            logger.info("Exercício encontrado: {}", exercicio.getDsExercicio());
            return ResponseEntity.ok(toResponse(exercicio));
            
        } catch (IllegalArgumentException e) {
            logger.error("Exercício não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar exercício", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/exercicio/grupo-muscular/{cdGrupoMuscular} - Listar exercícios de um grupo muscular
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return ResponseEntity<List<ExercicioResponse>>
     */
    @GetMapping("/grupo-muscular/{cdGrupoMuscular}")
    public ResponseEntity<?> listExercicio(@PathVariable Integer cdGrupoMuscular) {
        try {
            logger.info("Request para listar exercícios do grupo muscular ID: {}", cdGrupoMuscular);
            
            List<Exercicio> exercicios = exercicioService.listExercicio(cdGrupoMuscular);
            
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {}", exercicios.size(), cdGrupoMuscular);
            return ResponseEntity.ok(toResponseList(exercicios));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao listar exercícios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar exercícios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/exercicio/by-grupo-muscular/{cdGrupoMuscular} - Listar exercícios de um grupo muscular (método alternativo)
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return ResponseEntity<List<ExercicioResponse>>
     */
    @GetMapping("/by-grupo-muscular/{cdGrupoMuscular}")
    public ResponseEntity<?> listExercicioByGrupoMuscular(@PathVariable Integer cdGrupoMuscular) {
        try {
            logger.info("Request para listar exercícios do grupo muscular ID: {}", cdGrupoMuscular);
            
            List<Exercicio> exercicios = exercicioService.listExercicioByGrupoMuscular(cdGrupoMuscular);
            
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {}", exercicios.size(), cdGrupoMuscular);
            return ResponseEntity.ok(toResponseList(exercicios));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao listar exercícios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar exercícios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/exercicio/by-grupo-muscular/{cdGrupoMuscular}/search - Buscar exercícios por grupo muscular e nome
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @param dsExercicio Termo para busca no nome do exercício (query parameter)
     * @return ResponseEntity<List<ExercicioResponse>>
     */
    @GetMapping("/by-grupo-muscular/{cdGrupoMuscular}/search")
    public ResponseEntity<?> listExercicioByGrupoMuscularAndDsLikeExercicio(
            @PathVariable Integer cdGrupoMuscular,
            @RequestParam(name = "dsExercicio", required = true) String dsExercicio) {
        try {
            logger.info("Request para buscar exercícios do grupo muscular ID: {} com filtro: {}", 
                       cdGrupoMuscular, dsExercicio);
            
            List<Exercicio> exercicios = exercicioService.listExercicioByGrupoMuscularAndDsLikeExercicio(
                cdGrupoMuscular, dsExercicio);
            
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {} com filtro: {}", 
                       exercicios.size(), cdGrupoMuscular, dsExercicio);
            return ResponseEntity.ok(toResponseList(exercicios));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao buscar exercícios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar exercícios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
