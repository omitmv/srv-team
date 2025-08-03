package com.example.srvteam.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbTimeProfissional")
@IdClass(TimeProfissionalId.class)
public class TimeProfissional {

    @Id
    @NotNull(message = "Código do time é obrigatório")
    @Column(name = "cdTime")
    private Integer cdTime;

    @Id
    @NotNull(message = "Código do profissional é obrigatório")
    @Column(name = "cdProfissional")
    private Integer cdProfissional;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdTime", insertable = false, updatable = false)
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdProfissional", insertable = false, updatable = false)
    private Usuario profissional;

    // Construtores
    public TimeProfissional() {}

    public TimeProfissional(Integer cdTime, Integer cdProfissional) {
        this.cdTime = cdTime;
        this.cdProfissional = cdProfissional;
    }

    // Getters e Setters
    public Integer getCdTime() {
        return cdTime;
    }

    public void setCdTime(Integer cdTime) {
        this.cdTime = cdTime;
    }

    public Integer getCdProfissional() {
        return cdProfissional;
    }

    public void setCdProfissional(Integer cdProfissional) {
        this.cdProfissional = cdProfissional;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeProfissional that = (TimeProfissional) o;
        return Objects.equals(cdTime, that.cdTime) && Objects.equals(cdProfissional, that.cdProfissional);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdTime, cdProfissional);
    }

    @Override
    public String toString() {
        return "TimeProfissional{" +
                "cdTime=" + cdTime +
                ", cdProfissional=" + cdProfissional +
                '}';
    }
}
