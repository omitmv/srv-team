package com.example.srvteam.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.srvteam.catalogo.model.Pais;

public interface PaisRepository extends JpaRepository<Pais, Integer> {
}
