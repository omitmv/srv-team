package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.model.EstimuloExercicio;
import com.example.srvteam.model.EstimuloExercicioId;
import com.example.srvteam.repository.EstimuloExercicioRepository;
import com.example.srvteam.repository.EstimuloRepository;
import com.example.srvteam.repository.ExercicioRepository;
import com.example.srvteam.repository.TecnicaRepository;

import jakarta.transaction.Transactional;

/**
 * Service para EstimuloExercicio
 * Contém a lógica de negócio para operações com EstimuloExercicio
 */
@Service
public class EstimuloExercicioService {

    private static final Logger logger = LoggerFactory.getLogger(EstimuloExercicioService.class);

    @Autowired
    private EstimuloExercicioRepository estimuloExercicioRepository;

    @Autowired
    private EstimuloRepository estimuloRepository;

    @Autowired
    private ExercicioRepository exercicioRepository;

    @Autowired
    private TecnicaRepository tecnicaRepository;

    /**
     * Inserir novo EstimuloExercicio
     * 
     * @param estimuloExercicio EstimuloExercicio a ser inserido
     * @return EstimuloExercicio inserido
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    @Transactional
    public EstimuloExercicio insEstimuloExercicio(EstimuloExercicio estimuloExercicio) {
        logger.info("Inserindo novo EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                   estimuloExercicio.getCdEstimulo(), estimuloExercicio.getCdExercicio());

        // Validações
        if (estimuloExercicio.getCdEstimulo() == null) {
            throw new IllegalArgumentException("Código do estímulo é obrigatório");
        }

        if (estimuloExercicio.getCdExercicio() == null) {
            throw new IllegalArgumentException("Código do exercício é obrigatório");
        }

        if (estimuloExercicio.getCdTecnica() == null) {
            throw new IllegalArgumentException("Código da técnica é obrigatório");
        }

        // Verificar se o estímulo existe
        if (!estimuloRepository.existsById(estimuloExercicio.getCdEstimulo())) {
            throw new IllegalArgumentException("Estímulo não encontrado com código: " + estimuloExercicio.getCdEstimulo());
        }

        // Verificar se o exercício existe
        if (!exercicioRepository.existsById(estimuloExercicio.getCdExercicio())) {
            throw new IllegalArgumentException("Exercício não encontrado com código: " + estimuloExercicio.getCdExercicio());
        }

        // Verificar se a técnica existe
        if (!tecnicaRepository.existsById(estimuloExercicio.getCdTecnica())) {
            throw new IllegalArgumentException("Técnica não encontrada com código: " + estimuloExercicio.getCdTecnica());
        }

        // Verificar se já existe a relação
        if (estimuloExercicioRepository.existsByCdEstimuloAndCdExercicio(
                estimuloExercicio.getCdEstimulo(), estimuloExercicio.getCdExercicio())) {
            throw new IllegalArgumentException("Exercício já existe neste estímulo");
        }

        EstimuloExercicio novoEstimuloExercicio = estimuloExercicioRepository.save(estimuloExercicio);
        logger.info("EstimuloExercicio inserido com sucesso: cdEstimulo={}, cdExercicio={}", 
                   novoEstimuloExercicio.getCdEstimulo(), novoEstimuloExercicio.getCdExercicio());

        return novoEstimuloExercicio;
    }

    /**
     * Atualizar EstimuloExercicio existente
     * 
     * @param estimuloExercicio EstimuloExercicio com dados atualizados
     * @return EstimuloExercicio atualizado
     * @throws IllegalArgumentException se o EstimuloExercicio não for encontrado ou dados inválidos
     */
    @Transactional
    public EstimuloExercicio updEstimuloExercicio(EstimuloExercicio estimuloExercicio) {
        logger.info("Atualizando EstimuloExercicio: cdEstimulo={}, cdExercicio={}", 
                   estimuloExercicio.getCdEstimulo(), estimuloExercicio.getCdExercicio());

        // Validações
        if (estimuloExercicio.getCdEstimulo() == null) {
            throw new IllegalArgumentException("Código do estímulo é obrigatório");
        }

        if (estimuloExercicio.getCdExercicio() == null) {
            throw new IllegalArgumentException("Código do exercício é obrigatório");
        }

        if (estimuloExercicio.getCdTecnica() == null) {
            throw new IllegalArgumentException("Código da técnica é obrigatório");
        }

        // Verificar se existe
        EstimuloExercicioId id = new EstimuloExercicioId(estimuloExercicio.getCdEstimulo(), 
                                                        estimuloExercicio.getCdExercicio());
        Optional<EstimuloExercicio> estimuloExercicioExistente = estimuloExercicioRepository.findById(id);

        if (estimuloExercicioExistente.isEmpty()) {
            throw new IllegalArgumentException("EstimuloExercicio não encontrado");
        }

        // Verificar se a técnica existe
        if (!tecnicaRepository.existsById(estimuloExercicio.getCdTecnica())) {
            throw new IllegalArgumentException("Técnica não encontrada com código: " + estimuloExercicio.getCdTecnica());
        }

        EstimuloExercicio estimuloExercicioAtualizado = estimuloExercicioRepository.save(estimuloExercicio);
        logger.info("EstimuloExercicio atualizado com sucesso: cdEstimulo={}, cdExercicio={}", 
                   estimuloExercicioAtualizado.getCdEstimulo(), estimuloExercicioAtualizado.getCdExercicio());

        return estimuloExercicioAtualizado;
    }

    /**
     * Deletar EstimuloExercicio
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @throws IllegalArgumentException se o EstimuloExercicio não for encontrado
     */
    @Transactional
    public void delEstimuloExercicio(Integer cdEstimulo, Integer cdExercicio) {
        logger.info("Deletando EstimuloExercicio: cdEstimulo={}, cdExercicio={}", cdEstimulo, cdExercicio);

        if (cdEstimulo == null) {
            throw new IllegalArgumentException("Código do estímulo é obrigatório");
        }

        if (cdExercicio == null) {
            throw new IllegalArgumentException("Código do exercício é obrigatório");
        }

        EstimuloExercicioId id = new EstimuloExercicioId(cdEstimulo, cdExercicio);

        if (!estimuloExercicioRepository.existsById(id)) {
            throw new IllegalArgumentException("EstimuloExercicio não encontrado");
        }

        estimuloExercicioRepository.deleteById(id);
        logger.info("EstimuloExercicio deletado com sucesso: cdEstimulo={}, cdExercicio={}", cdEstimulo, cdExercicio);
    }

    /**
     * Obter EstimuloExercicio pelo código do estímulo e código do exercício
     * 
     * @param cdEstimulo Código do estímulo
     * @param cdExercicio Código do exercício
     * @return EstimuloExercicio encontrado
     * @throws IllegalArgumentException se o EstimuloExercicio não for encontrado
     */
    public EstimuloExercicio getEstimuloExercicio(Integer cdEstimulo, Integer cdExercicio) {
        logger.info("Buscando EstimuloExercicio: cdEstimulo={}, cdExercicio={}", cdEstimulo, cdExercicio);

        if (cdEstimulo == null) {
            throw new IllegalArgumentException("Código do estímulo é obrigatório");
        }

        if (cdExercicio == null) {
            throw new IllegalArgumentException("Código do exercício é obrigatório");
        }

        Optional<EstimuloExercicio> estimuloExercicio = estimuloExercicioRepository
                .findByCdEstimuloAndCdExercicio(cdEstimulo, cdExercicio);

        if (estimuloExercicio.isEmpty()) {
            throw new IllegalArgumentException("EstimuloExercicio não encontrado");
        }

        logger.info("EstimuloExercicio encontrado: cdEstimulo={}, cdExercicio={}", cdEstimulo, cdExercicio);
        return estimuloExercicio.get();
    }

    /**
     * Listar todos os exercícios de um estímulo específico
     * 
     * @param cdEstimulo Código do estímulo
     * @return Lista de EstimuloExercicio
     * @throws IllegalArgumentException se o código do estímulo for inválido
     */
    public List<EstimuloExercicio> listEstimuloExercicioByEstimulo(Integer cdEstimulo) {
        logger.info("Listando EstimuloExercicio por estímulo: {}", cdEstimulo);

        if (cdEstimulo == null) {
            throw new IllegalArgumentException("Código do estímulo é obrigatório");
        }

        // Verificar se o estímulo existe
        if (!estimuloRepository.existsById(cdEstimulo)) {
            throw new IllegalArgumentException("Estímulo não encontrado com código: " + cdEstimulo);
        }

        List<EstimuloExercicio> estimuloExercicios = estimuloExercicioRepository
                .findByCdEstimuloOrderByExercicioDesc(cdEstimulo);

        logger.info("Encontrados {} EstimuloExercicio para estímulo: {}", estimuloExercicios.size(), cdEstimulo);
        return estimuloExercicios;
    }
}
