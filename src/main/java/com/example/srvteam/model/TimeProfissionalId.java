package com.example.srvteam.model;

import java.io.Serializable;
import java.util.Objects;

public class TimeProfissionalId implements Serializable {

    private Integer cdTime;
    private Integer cdProfissional;

    // Construtores
    public TimeProfissionalId() {}

    public TimeProfissionalId(Integer cdTime, Integer cdProfissional) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeProfissionalId that = (TimeProfissionalId) o;
        return Objects.equals(cdTime, that.cdTime) && Objects.equals(cdProfissional, that.cdProfissional);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdTime, cdProfissional);
    }

    @Override
    public String toString() {
        return "TimeProfissionalId{" +
                "cdTime=" + cdTime +
                ", cdProfissional=" + cdProfissional +
                '}';
    }
}
