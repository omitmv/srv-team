package com.example.srvteam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.srvteam.model.Competidores;
import com.example.srvteam.model.CompetidoresId;

@Repository
public interface CompetidoresRepository extends JpaRepository<Competidores, CompetidoresId> {
    List<Competidores> findByCdCompeticao(Integer cdCompeticao);
}
