package com.example.srvteam.campeonato.repository;

import com.example.srvteam.campeonato.model.Campeonato;
import com.example.srvteam.campeonato.model.CampeonatoStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Integer> {

    boolean existsByNmCompeticaoNormalizadoAndOrganizadorCdOrganizadorAndPaisCdPaisAndSubdivisaoCdSubdivisaoAndDtInicio(
            String nmCompeticaoNormalizado,
            Integer cdOrganizador,
            Integer cdPais,
            Integer cdSubdivisao,
            LocalDate dtInicio);

    boolean existsByNmCompeticaoNormalizadoAndOrganizadorCdOrganizadorAndPaisCdPaisAndSubdivisaoCdSubdivisaoAndDtInicioAndStatus(
            String nmCompeticaoNormalizado,
            Integer cdOrganizador,
            Integer cdPais,
            Integer cdSubdivisao,
            LocalDate dtInicio,
            CampeonatoStatus status);

    boolean existsByNmCompeticaoNormalizadoAndOrganizadorCdOrganizadorAndPaisCdPaisAndSubdivisaoIsNullAndDtInicio(
            String nmCompeticaoNormalizado,
            Integer cdOrganizador,
            Integer cdPais,
            LocalDate dtInicio);

    boolean existsByNmCompeticaoNormalizadoAndOrganizadorCdOrganizadorAndPaisCdPaisAndSubdivisaoIsNullAndDtInicioAndStatus(
            String nmCompeticaoNormalizado,
            Integer cdOrganizador,
            Integer cdPais,
            LocalDate dtInicio,
            CampeonatoStatus status);
}
