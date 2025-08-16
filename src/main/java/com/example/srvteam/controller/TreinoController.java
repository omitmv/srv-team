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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.TreinoRequest;
import com.example.srvteam.dto.response.TreinoResponse;
import com.example.srvteam.model.Treino;
import com.example.srvteam.service.TreinoService;

import jakarta.validation.Valid;

/**
 * Controller para Treino
 * Endpoints para operações CRUD de Treino
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/treino")
@CrossOrigin(origins = "*")
public class TreinoController {

    private static final Logger logger = LoggerFactory.getLogger(TreinoController.class);

    @Autowired
    private TreinoService treinoService;

    /**
     * Converter Treino para TreinoResponse
     */
    private TreinoResponse toResponse(Treino treino) {
        String nomeProfissional = null;
        String nomeAtleta = null;
        
        if (treino.getProfissional() != null) {
            nomeProfissional = treino.getProfissional().getNome();
        }
        
        if (treino.getAtleta() != null) {
            nomeAtleta = treino.getAtleta().getNome();
        }
        
        return new TreinoResponse(
                treino.getCdTreino(),
                treino.getDsTreino(),
                treino.getDtCadastro(),
                treino.getDtInicio(),
                treino.getDtFinal(),
                treino.getCdProfissional(),
                nomeProfissional,
                treino.getCdAtleta(),
                nomeAtleta,
                treino.getObs()
        );
    }

    /**
     * Converter TreinoRequest para Treino
     */
    private Treino fromRequest(TreinoRequest request) {
        Treino treino = new Treino();
        treino.setDsTreino(request.getDsTreino());
        treino.setDtInicio(request.getDtInicio());
        treino.setDtFinal(request.getDtFinal());
        treino.setCdProfissional(request.getCdProfissional());
        treino.setCdAtleta(request.getCdAtleta());
        treino.setObs(request.getObs());
        return treino;
    }

    /**
     * Converter lista de Treino para lista de TreinoResponse
     */
    private List<TreinoResponse> toResponseList(List<Treino> treinos) {
        return treinos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/treino - Inserir novo treino
     * 
     * @param treinoRequest Dados do treino a ser criado
     * @return ResponseEntity<TreinoResponse>
     */
    @PostMapping
    public ResponseEntity<?> insTreino(@Valid @RequestBody TreinoRequest treinoRequest) {
        try {
            logger.info("Request para inserir novo treino: {}", treinoRequest.getDsTreino());
            
            Treino treino = fromRequest(treinoRequest);
            Treino novoTreino = treinoService.insTreino(treino);
            
            logger.info("Treino criado com sucesso. ID: {}", novoTreino.getCdTreino());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoTreino));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir treino: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro interno ao inserir treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/treino/{cdTreino} - Atualizar treino existente
     * 
     * @param cdTreino ID do treino a ser atualizado
     * @param treinoRequest Dados atualizados do treino
     * @return ResponseEntity<TreinoResponse>
     */
    @PutMapping("/{cdTreino}")
    public ResponseEntity<?> updTreino(@PathVariable Integer cdTreino, 
                                      @Valid @RequestBody TreinoRequest treinoRequest) {
        try {
            logger.info("Request para atualizar treino: {}", cdTreino);
            
            Treino treino = fromRequest(treinoRequest);
            treino.setCdTreino(cdTreino);
            
            Treino treinoAtualizado = treinoService.updTreino(treino);
            
            logger.info("Treino atualizado com sucesso. ID: {}", treinoAtualizado.getCdTreino());
            return ResponseEntity.ok(toResponse(treinoAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar treino: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/treino/{cdTreino} - Deletar treino
     * 
     * @param cdTreino ID do treino a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdTreino}")
    public ResponseEntity<?> delTreino(@PathVariable Integer cdTreino) {
        try {
            logger.info("Request para deletar treino: {}", cdTreino);
            
            treinoService.delTreino(cdTreino);
            
            logger.info("Treino deletado com sucesso. ID: {}", cdTreino);
            return ResponseEntity.ok("Treino deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar treino: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro interno ao deletar treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino/{cdTreino} - Obter treino por ID
     * 
     * @param cdTreino ID do treino
     * @return ResponseEntity<TreinoResponse>
     */
    @GetMapping("/{cdTreino}")
    public ResponseEntity<?> getTreino(@PathVariable Integer cdTreino) {
        try {
            logger.info("Request para buscar treino: {}", cdTreino);
            
            Optional<Treino> treino = treinoService.getTreino(cdTreino);
            
            if (treino.isPresent()) {
                logger.info("Treino encontrado: {}", cdTreino);
                return ResponseEntity.ok(toResponse(treino.get()));
            } else {
                logger.warn("Treino não encontrado: {}", cdTreino);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar treino", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino/atleta/{cdAtleta} - Listar treinos por atleta
     * 
     * @param cdAtleta ID do atleta
     * @return ResponseEntity<List<TreinoResponse>>
     */
    @GetMapping("/atleta/{cdAtleta}")
    public ResponseEntity<?> listTreinoByAtleta(@PathVariable Integer cdAtleta) {
        try {
            logger.info("Request para listar treinos do atleta: {}", cdAtleta);
            
            List<Treino> treinos = treinoService.listTreinoByAtleta(cdAtleta);
            
            logger.info("Encontrados {} treinos para o atleta: {}", treinos.size(), cdAtleta);
            return ResponseEntity.ok(toResponseList(treinos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar treinos por atleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino/profissional/{cdProfissional} - Listar treinos por profissional
     * 
     * @param cdProfissional ID do profissional
     * @return ResponseEntity<List<TreinoResponse>>
     */
    @GetMapping("/profissional/{cdProfissional}")
    public ResponseEntity<?> listTreinoByProfissional(@PathVariable Integer cdProfissional) {
        try {
            logger.info("Request para listar treinos do profissional: {}", cdProfissional);
            
            List<Treino> treinos = treinoService.listTreinoByProfissional(cdProfissional);
            
            logger.info("Encontrados {} treinos para o profissional: {}", treinos.size(), cdProfissional);
            return ResponseEntity.ok(toResponseList(treinos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar treinos por profissional", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/treino - Listar treinos por atleta e profissional
     * 
     * @param cdAtleta ID do atleta
     * @param cdProfissional ID do profissional
     * @return ResponseEntity<List<TreinoResponse>>
     */
    @GetMapping
    public ResponseEntity<?> listTreinoByAtletaAndProfissional(
            @RequestParam Integer cdAtleta, 
            @RequestParam Integer cdProfissional) {
        try {
            logger.info("Request para listar treinos do atleta {} e profissional {}", cdAtleta, cdProfissional);
            
            List<Treino> treinos = treinoService.listTreinoByAtletaAndProfissional(cdAtleta, cdProfissional);
            
            logger.info("Encontrados {} treinos para o atleta {} e profissional {}", 
                       treinos.size(), cdAtleta, cdProfissional);
            return ResponseEntity.ok(toResponseList(treinos));
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar treinos por atleta e profissional", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
