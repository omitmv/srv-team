package com.example.srvteam.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbCompetidores")
@IdClass(CompetidoresId.class)
public class Competidores implements Serializable {
    @Id
    @Column(name = "cdCompetidor", nullable = false)
    private Integer cdCompetidor;

    @Id
    @Column(name = "cdCompeticao", nullable = false)
    private Integer cdCompeticao;

    @Column(name = "dtCadastro", nullable = false)
    private LocalDateTime dtCadastro = LocalDateTime.now();

    // Getters e Setters
    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }

    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }

    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }
}
