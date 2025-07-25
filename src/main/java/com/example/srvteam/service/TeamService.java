package com.example.srvteam.service;

import com.example.srvteam.model.Team;
import com.example.srvteam.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    
    @Autowired
    private TeamRepository teamRepository;
    
    // Listar todos os times
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    // Buscar time por ID
    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }
    
    // Buscar time por nome
    public Optional<Team> getTeamByName(String name) {
        return teamRepository.findByNameIgnoreCase(name);
    }
    
    // Criar novo time
    public Team createTeam(Team team) {
        // Verificar se já existe um time com esse nome
        if (teamRepository.existsByNameIgnoreCase(team.getName())) {
            throw new IllegalArgumentException("Já existe um time com esse nome: " + team.getName());
        }
        return teamRepository.save(team);
    }
    
    // Atualizar time existente
    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Time não encontrado com ID: " + id));
        
        // Verificar se o novo nome já está em uso por outro time
        if (!team.getName().equalsIgnoreCase(teamDetails.getName()) && 
            teamRepository.existsByNameIgnoreCase(teamDetails.getName())) {
            throw new IllegalArgumentException("Já existe um time com esse nome: " + teamDetails.getName());
        }
        
        team.setName(teamDetails.getName());
        team.setDescription(teamDetails.getDescription());
        team.setEmail(teamDetails.getEmail());
        team.setTeamLead(teamDetails.getTeamLead());
        
        return teamRepository.save(team);
    }
    
    // Deletar time
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Time não encontrado com ID: " + id));
        teamRepository.delete(team);
    }
    
    // Buscar times por nome (busca parcial)
    public List<Team> searchTeamsByName(String name) {
        return teamRepository.findByNameContainingIgnoreCase(name);
    }
    
    // Buscar times por team lead
    public List<Team> getTeamsByLead(String teamLead) {
        return teamRepository.findByTeamLead(teamLead);
    }
    
    // Buscar times por palavra-chave na descrição
    public List<Team> searchTeamsByDescription(String keyword) {
        return teamRepository.findByDescriptionContaining(keyword);
    }
}
