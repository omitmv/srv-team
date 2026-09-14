package com.example.srvteam.catalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.srvteam.catalogo.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNmCategoriaNormalizadoAndFlAtivoTrue(String nmCategoriaNormalizado);
}
