package com.example.srvteam.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.example.srvteam.dto.request.TimeProfissionalRequest;
import com.example.srvteam.dto.response.TimeProfissionalResponse;
import com.example.srvteam.model.TimeProfissional;
import com.example.srvteam.service.TimeProfissionalService;
import com.example.srvteam.util.TimeProfissionalMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/time-profissional")
@CrossOrigin(origins = "*")
public class TimeProfissionalController {

    @Autowired
    private TimeProfissionalService timeProfissionalService;

    /**
     * POST /v1/time-profissional - Inserir novo profissional ao time
     */
    @PostMapping
    public ResponseEntity<?> insTimeProfissional(@Valid @RequestBody TimeProfissionalRequest request) {
        try {
            TimeProfissional timeProfissional = TimeProfissionalMapper.toEntity(request);
            TimeProfissional timeProfissionalCriado = timeProfissionalService.insTimeProfissional(timeProfissional);
            TimeProfissionalResponse response = TimeProfissionalMapper.toResponse(timeProfissionalCriado);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /v1/time-profissional/{cdTime}/{cdProfissional} - Deletar profissional do time
     */
    @DeleteMapping("/{cdTime}/{cdProfissional}")
    public ResponseEntity<?> delTimeProfissional(@PathVariable Integer cdTime, 
                                                @PathVariable Integer cdProfissional) {
        try {
            timeProfissionalService.delTimeProfissional(cdTime, cdProfissional);
            return ResponseEntity.ok().body("Profissional removido do time com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /v1/time-profissional/{cdTime}/{cdProfissional} - Obter profissional do time
     */
    @GetMapping("/{cdTime}/{cdProfissional}")
    public ResponseEntity<TimeProfissionalResponse> getTimeProfissional(@PathVariable Integer cdTime,
                                                                       @PathVariable Integer cdProfissional) {
        Optional<TimeProfissional> timeProfissional = timeProfissionalService.getTimeProfissional(cdTime, cdProfissional);
        if (timeProfissional.isPresent()) {
            TimeProfissionalResponse response = TimeProfissionalMapper.toResponse(timeProfissional.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /v1/time-profissional/time/{cdTime} - Listar profissionais de um time
     */
    @GetMapping("/time/{cdTime}")
    public ResponseEntity<?> listTimeProfissionalByTime(@PathVariable Integer cdTime) {
        try {
            List<TimeProfissional> timeProfissionais = timeProfissionalService.listTimeProfissionalByTime(cdTime);
            List<TimeProfissionalResponse> response = timeProfissionais.stream()
                    .map(TimeProfissionalMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /v1/time-profissional/profissional/{cdProfissional} - Listar times de um profissional
     */
    @GetMapping("/profissional/{cdProfissional}")
    public ResponseEntity<?> listTimeProfissionalByProfissional(@PathVariable Integer cdProfissional) {
        try {
            List<TimeProfissional> timeProfissionais = timeProfissionalService.listTimeProfissionalByProfissional(cdProfissional);
            List<TimeProfissionalResponse> response = timeProfissionais.stream()
                    .map(TimeProfissionalMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /v1/time-profissional/time/{cdTime}/count - Contar profissionais em um time
     */
    @GetMapping("/time/{cdTime}/count")
    public ResponseEntity<Long> countProfissionaisNoTime(@PathVariable Integer cdTime) {
        long count = timeProfissionalService.countProfissionaisNoTime(cdTime);
        return ResponseEntity.ok(count);
    }

    /**
     * GET /v1/time-profissional/profissional/{cdProfissional}/count - Contar times de um profissional
     */
    @GetMapping("/profissional/{cdProfissional}/count")
    public ResponseEntity<Long> countTimesDosProfissional(@PathVariable Integer cdProfissional) {
        long count = timeProfissionalService.countTimesDosProfissional(cdProfissional);
        return ResponseEntity.ok(count);
    }

    /**
     * GET /v1/time-profissional/existe/{cdTime}/{cdProfissional} - Verificar se profissional está no time
     */
    @GetMapping("/existe/{cdTime}/{cdProfissional}")
    public ResponseEntity<Boolean> isProfissionalNoTime(@PathVariable Integer cdTime,
                                                       @PathVariable Integer cdProfissional) {
        boolean exists = timeProfissionalService.isProfissionalNoTime(cdTime, cdProfissional);
        return ResponseEntity.ok(exists);
    }

    /**
     * DELETE /v1/time-profissional/time/{cdTime}/todos - Remover todos os profissionais de um time
     */
    @DeleteMapping("/time/{cdTime}/todos")
    public ResponseEntity<?> removeAllProfissionaisDoTime(@PathVariable Integer cdTime) {
        try {
            timeProfissionalService.removeAllProfissionaisDoTime(cdTime);
            return ResponseEntity.ok().body("Todos os profissionais foram removidos do time com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
