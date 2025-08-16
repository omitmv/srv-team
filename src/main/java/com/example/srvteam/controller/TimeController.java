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

import com.example.srvteam.dto.request.TimeRequest;
import com.example.srvteam.dto.response.TimeResponse;
import com.example.srvteam.model.Time;
import com.example.srvteam.service.TimeService;
import com.example.srvteam.util.TimeMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/time")
@CrossOrigin(origins = "*")
public class TimeController {

    @Autowired
    private TimeService timeService;

    /**
     * POST /v1/time - Inserir novo time
     */
    @PostMapping
    public ResponseEntity<?> insTime(@Valid @RequestBody TimeRequest timeRequest) {
        try {
            Time time = TimeMapper.toEntity(timeRequest);
            Time timeCriado = timeService.insTime(time);
            TimeResponse response = TimeMapper.toResponse(timeCriado);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /v1/time/{cdTime} - Deletar time
     */
    @DeleteMapping("/{cdTime}")
    public ResponseEntity<?> delTime(@PathVariable Integer cdTime) {
        try {
            timeService.delTime(cdTime);
            return ResponseEntity.ok().body("Time deletado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /v1/time/{cdTime} - Obter time por ID
     */
    @GetMapping("/{cdTime}")
    public ResponseEntity<TimeResponse> getTime(@PathVariable Integer cdTime) {
        Optional<Time> time = timeService.getTime(cdTime);
        if (time.isPresent()) {
            TimeResponse response = TimeMapper.toResponse(time.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /v1/time/profissional/{cdProfissionalResponsavel} - Listar times por profissional responsável
     */
    @GetMapping("/profissional/{cdProfissionalResponsavel}")
    public ResponseEntity<?> listTime(@PathVariable Integer cdProfissionalResponsavel) {
        try {
            List<Time> times = timeService.listTime(cdProfissionalResponsavel);
            List<TimeResponse> response = times.stream()
                    .map(TimeMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /v1/time - Listar todos os times
     */
    @GetMapping
    public ResponseEntity<List<TimeResponse>> getAllTimes() {
        List<Time> times = timeService.getAllTimes();
        List<TimeResponse> response = times.stream()
                .map(TimeMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /v1/time/buscar/{nome} - Buscar times por nome
     */
    @GetMapping("/buscar/{nome}")
    public ResponseEntity<List<TimeResponse>> findTimesByNome(@PathVariable String nome) {
        List<Time> times = timeService.findTimesByNome(nome);
        List<TimeResponse> response = times.stream()
                .map(TimeMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /v1/time/existe/{nome} - Verificar se time existe por nome
     */
    @GetMapping("/existe/{nome}")
    public ResponseEntity<Boolean> existsByNome(@PathVariable String nome) {
        boolean existe = timeService.existsByNome(nome);
        return ResponseEntity.ok(existe);
    }
}
