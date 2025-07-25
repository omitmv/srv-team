package com.example.srvteam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "teams")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;
    
    @Email(message = "Email deve ser válido")
    private String email;
    
    @Column(name = "team_lead")
    private String teamLead;
    
    // Construtores
    public Team() {}
    
    public Team(String name, String description, String email, String teamLead) {
        this.name = name;
        this.description = description;
        this.email = email;
        this.teamLead = teamLead;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTeamLead() {
        return teamLead;
    }
    
    public void setTeamLead(String teamLead) {
        this.teamLead = teamLead;
    }
    
    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                ", teamLead='" + teamLead + '\'' +
                '}';
    }
}
