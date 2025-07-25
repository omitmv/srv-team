package com.example.srvteam.controller;

import com.example.srvteam.model.Team;
import com.example.srvteam.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @Autowired
    private ObjectMapper objectMapper;

    private Team sampleTeam;

    @BeforeEach
    void setUp() {
        sampleTeam = new Team();
        sampleTeam.setId(1L);
        sampleTeam.setName("Desenvolvimento");
        sampleTeam.setDescription("Time de desenvolvimento de software");
        sampleTeam.setEmail("dev@example.com");
        sampleTeam.setTeamLead("João Silva");
    }

    @Test
    void testGetAllTeams() throws Exception {
        when(teamService.getAllTeams()).thenReturn(Arrays.asList(sampleTeam));

        mockMvc.perform(get("/v1/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Desenvolvimento"))
                .andExpect(jsonPath("$[0].email").value("dev@example.com"));
    }

    @Test
    void testGetTeamById() throws Exception {
        when(teamService.getTeamById(1L)).thenReturn(Optional.of(sampleTeam));

        mockMvc.perform(get("/v1/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Desenvolvimento"));
    }

    @Test
    void testGetTeamByIdNotFound() throws Exception {
        when(teamService.getTeamById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/teams/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTeam() throws Exception {
        when(teamService.createTeam(any(Team.class))).thenReturn(sampleTeam);

        mockMvc.perform(post("/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTeam)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Desenvolvimento"));
    }

    @Test
    void testDeleteTeam() throws Exception {
        mockMvc.perform(delete("/v1/teams/1"))
                .andExpect(status().isOk());
    }
}
