package com.example.srvteam.controller;

import com.example.srvteam.model.Team;
import com.example.srvteam.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    // GET /api/teams - Listar todos os times
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }
    
    // GET /api/teams/{id} - Buscar time por ID
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        return team.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    // GET /api/teams/search?name={name} - Buscar times por nome
    @GetMapping("/search")
    public ResponseEntity<List<Team>> searchTeams(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String lead,
                                                  @RequestParam(required = false) String description) {
        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(teamService.searchTeamsByName(name));
        } else if (lead != null && !lead.isEmpty()) {
            return ResponseEntity.ok(teamService.getTeamsByLead(lead));
        } else if (description != null && !description.isEmpty()) {
            return ResponseEntity.ok(teamService.searchTeamsByDescription(description));
        } else {
            return ResponseEntity.ok(teamService.getAllTeams());
        }
    }
    
    // POST /api/teams - Criar novo time
    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody Team team) {
        try {
            Team createdTeam = teamService.createTeam(team);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // PUT /api/teams/{id} - Atualizar time existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @Valid @RequestBody Team teamDetails) {
        try {
            Team updatedTeam = teamService.updateTeam(id, teamDetails);
            return ResponseEntity.ok(updatedTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // DELETE /api/teams/{id} - Deletar time
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.ok().body("Time deletado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/teams/name/{name} - Buscar time específico por nome
    @GetMapping("/name/{name}")
    public ResponseEntity<Team> getTeamByName(@PathVariable String name) {
        Optional<Team> team = teamService.getTeamByName(name);
        return team.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
}
