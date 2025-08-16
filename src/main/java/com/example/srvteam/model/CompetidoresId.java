package com.example.srvteam.model;

import java.io.Serializable;
import java.util.Objects;

public class CompetidoresId implements Serializable {
    private Integer cdCompetidor;
    private Integer cdCompeticao;

    public CompetidoresId() {}
    public CompetidoresId(Integer cdCompetidor, Integer cdCompeticao) {
        this.cdCompetidor = cdCompetidor;
        this.cdCompeticao = cdCompeticao;
    }
    public Integer getCdCompetidor() { return cdCompetidor; }
    public void setCdCompetidor(Integer cdCompetidor) { this.cdCompetidor = cdCompetidor; }
    public Integer getCdCompeticao() { return cdCompeticao; }
    public void setCdCompeticao(Integer cdCompeticao) { this.cdCompeticao = cdCompeticao; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetidoresId that = (CompetidoresId) o;
        return Objects.equals(cdCompetidor, that.cdCompetidor) && Objects.equals(cdCompeticao, that.cdCompeticao);
    }
    @Override
    public int hashCode() {
        return Objects.hash(cdCompetidor, cdCompeticao);
    }
}
