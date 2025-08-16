package com.example.srvteam.repository;

import com.example.srvteam.model.Competidores;
import com.example.srvteam.model.CompetidoresId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetidoresRepository extends JpaRepository<Competidores, CompetidoresId> {
    List<Competidores> findByCdCompeticao(Integer cdCompeticao);
}
