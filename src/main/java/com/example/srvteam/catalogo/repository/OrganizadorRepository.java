package com.example.srvteam.catalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.srvteam.catalogo.model.Organizador;

public interface OrganizadorRepository extends JpaRepository<Organizador, Integer> {
    Optional<Organizador> findByNmOrganizadorNormalizadoAndFlAtivoTrue(String nmOrganizadorNormalizado);
}
