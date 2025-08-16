package com.example.srvteam.dto.response;

import java.time.LocalDateTime;

public class CompetidorResponse {
    private Integer cdCompetidor;
    private Integer cdCompeticao;
    private LocalDateTime dtCadastro;

    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }
}
