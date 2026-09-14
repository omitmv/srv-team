package com.example.srvteam.catalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.srvteam.catalogo.model.Classe;
import com.example.srvteam.catalogo.model.TipoClasse;

public interface ClasseRepository extends JpaRepository<Classe, Integer> {
    Optional<Classe> findByCategoriaCdCategoriaAndNmClasseNormalizadoAndFlAtivoTrue(
            Integer cdCategoria, String nmClasseNormalizado);

    Optional<Classe> findByCategoriaCdCategoriaAndTipoClasseAndFlAtivoTrue(
            Integer cdCategoria, TipoClasse tipoClasse);
}
