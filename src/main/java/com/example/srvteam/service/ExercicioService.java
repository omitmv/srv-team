package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Exercicio;
import com.example.srvteam.repository.ExercicioRepository;
import com.example.srvteam.repository.GrupoMuscularRepository;

/**
 * Service para Exercicio
 * Contém a lógica de negócio para operações com Exercicio
 */
@Service
@Transactional
public class ExercicioService {

    private static final Logger logger = LoggerFactory.getLogger(ExercicioService.class);

    @Autowired
    private ExercicioRepository exercicioRepository;
    
    @Autowired
    private GrupoMuscularRepository grupoMuscularRepository;

    /**
     * Inserir novo exercício
     * 
     * @param exercicio Dados do exercício
     * @return Exercicio criado
     * @throws IllegalArgumentException se já existe exercício com a mesma descrição ou grupo muscular não existe
     */
    public Exercicio insExercicio(Exercicio exercicio) {
        logger.info("Iniciando inserção de novo exercício: {}", exercicio.getDsExercicio());
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(exercicio.getCdGrupoMuscular())) {
            logger.error("Tentativa de inserir exercício com grupo muscular inexistente: {}", exercicio.getCdGrupoMuscular());
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + exercicio.getCdGrupoMuscular());
        }
        
        // Verificar se já existe exercício com essa descrição
        if (exercicioRepository.existsByDsExercicio(exercicio.getDsExercicio())) {
            logger.error("Tentativa de inserir exercício com descrição já existente: {}", exercicio.getDsExercicio());
            throw new IllegalArgumentException("Já existe um exercício com a descrição: " + exercicio.getDsExercicio());
        }
        
        try {
            Exercicio exercicioSalvo = exercicioRepository.save(exercicio);
            logger.info("Exercício inserido com sucesso. ID: {}", exercicioSalvo.getCdExercicio());
            return exercicioSalvo;
        } catch (Exception e) {
            logger.error("Erro ao inserir exercício: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao inserir exercício");
        }
    }

    /**
     * Atualizar exercício existente
     * 
     * @param exercicio Dados do exercício para atualização
     * @return Exercicio atualizado
     * @throws IllegalArgumentException se exercício não existe, grupo muscular não existe ou descrição já existe para outro exercício
     */
    public Exercicio updExercicio(Exercicio exercicio) {
        logger.info("Iniciando atualização do exercício ID: {}", exercicio.getCdExercicio());
        
        // Verificar se o exercício existe
        Optional<Exercicio> exercicioExistente = exercicioRepository.findById(exercicio.getCdExercicio());
        if (exercicioExistente.isEmpty()) {
            logger.error("Tentativa de atualizar exercício inexistente. ID: {}", exercicio.getCdExercicio());
            throw new IllegalArgumentException("Exercício não encontrado com o código: " + exercicio.getCdExercicio());
        }
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(exercicio.getCdGrupoMuscular())) {
            logger.error("Tentativa de atualizar exercício com grupo muscular inexistente: {}", exercicio.getCdGrupoMuscular());
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + exercicio.getCdGrupoMuscular());
        }
        
        // Verificar se já existe outro exercício com essa descrição
        if (exercicioRepository.existsByDsExercicioAndCdExercicioNot(exercicio.getDsExercicio(), exercicio.getCdExercicio())) {
            logger.error("Tentativa de atualizar exercício com descrição já existente: {}", exercicio.getDsExercicio());
            throw new IllegalArgumentException("Já existe outro exercício com a descrição: " + exercicio.getDsExercicio());
        }
        
        try {
            Exercicio exercicioAtualizado = exercicioRepository.save(exercicio);
            logger.info("Exercício atualizado com sucesso. ID: {}", exercicioAtualizado.getCdExercicio());
            return exercicioAtualizado;
        } catch (Exception e) {
            logger.error("Erro ao atualizar exercício: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar exercício");
        }
    }

    /**
     * Deletar exercício
     * 
     * @param cdExercicio Código do exercício
     * @throws IllegalArgumentException se exercício não existe
     */
    public void delExercicio(Integer cdExercicio) {
        logger.info("Iniciando exclusão do exercício ID: {}", cdExercicio);
        
        // Verificar se o exercício existe
        if (!exercicioRepository.existsById(cdExercicio)) {
            logger.error("Tentativa de deletar exercício inexistente. ID: {}", cdExercicio);
            throw new IllegalArgumentException("Exercício não encontrado com o código: " + cdExercicio);
        }
        
        try {
            exercicioRepository.deleteById(cdExercicio);
            logger.info("Exercício deletado com sucesso. ID: {}", cdExercicio);
        } catch (Exception e) {
            logger.error("Erro ao deletar exercício: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar exercício");
        }
    }

    /**
     * Obter exercício por código
     * 
     * @param cdExercicio Código do exercício
     * @return Exercicio encontrado
     * @throws IllegalArgumentException se exercício não existe
     */
    @Transactional(readOnly = true)
    public Exercicio getExercicio(Integer cdExercicio) {
        logger.info("Buscando exercício por ID: {}", cdExercicio);
        
        Optional<Exercicio> exercicio = exercicioRepository.findById(cdExercicio);
        if (exercicio.isEmpty()) {
            logger.error("Exercício não encontrado. ID: {}", cdExercicio);
            throw new IllegalArgumentException("Exercício não encontrado com o código: " + cdExercicio);
        }
        
        logger.info("Exercício encontrado: {}", exercicio.get().getDsExercicio());
        return exercicio.get();
    }

    /**
     * Listar todos os exercícios de um grupo muscular específico
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return Lista de exercícios do grupo muscular
     * @throws IllegalArgumentException se grupo muscular não existe
     */
    @Transactional(readOnly = true)
    public List<Exercicio> listExercicio(Integer cdGrupoMuscular) {
        logger.info("Listando exercícios do grupo muscular ID: {}", cdGrupoMuscular);
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(cdGrupoMuscular)) {
            logger.error("Tentativa de listar exercícios de grupo muscular inexistente: {}", cdGrupoMuscular);
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + cdGrupoMuscular);
        }
        
        try {
            List<Exercicio> exercicios = exercicioRepository.findByCdGrupoMuscularOrderByDsExercicio(cdGrupoMuscular);
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {}", exercicios.size(), cdGrupoMuscular);
            return exercicios;
        } catch (Exception e) {
            logger.error("Erro ao listar exercícios do grupo muscular: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar exercícios do grupo muscular");
        }
    }

    /**
     * Listar todos os exercícios de um grupo muscular específico (método alternativo)
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return Lista de exercícios do grupo muscular
     * @throws IllegalArgumentException se grupo muscular não existe
     */
    @Transactional(readOnly = true)
    public List<Exercicio> listExercicioByGrupoMuscular(Integer cdGrupoMuscular) {
        logger.info("Listando exercícios do grupo muscular ID: {}", cdGrupoMuscular);
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(cdGrupoMuscular)) {
            logger.error("Tentativa de listar exercícios de grupo muscular inexistente: {}", cdGrupoMuscular);
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + cdGrupoMuscular);
        }
        
        try {
            List<Exercicio> exercicios = exercicioRepository.findByCdGrupoMuscularOrderByDsExercicio(cdGrupoMuscular);
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {}", exercicios.size(), cdGrupoMuscular);
            return exercicios;
        } catch (Exception e) {
            logger.error("Erro ao listar exercícios do grupo muscular: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar exercícios do grupo muscular");
        }
    }

    /**
     * Listar exercícios de um grupo muscular específico filtrando pelo nome do exercício
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @param dsExercicio Termo para busca no nome do exercício (usando LIKE)
     * @return Lista de exercícios filtrados
     * @throws IllegalArgumentException se grupo muscular não existe
     */
    @Transactional(readOnly = true)
    public List<Exercicio> listExercicioByGrupoMuscularAndDsLikeExercicio(Integer cdGrupoMuscular, String dsExercicio) {
        logger.info("Listando exercícios do grupo muscular ID: {} com filtro de nome: {}", cdGrupoMuscular, dsExercicio);
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(cdGrupoMuscular)) {
            logger.error("Tentativa de listar exercícios de grupo muscular inexistente: {}", cdGrupoMuscular);
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + cdGrupoMuscular);
        }
        
        try {
            List<Exercicio> exercicios = exercicioRepository.findByCdGrupoMuscularAndDsExercicioContainingIgnoreCase(cdGrupoMuscular, dsExercicio);
            logger.info("Encontrados {} exercícios para o grupo muscular ID: {} com filtro: {}", exercicios.size(), cdGrupoMuscular, dsExercicio);
            return exercicios;
        } catch (Exception e) {
            logger.error("Erro ao listar exercícios filtrados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar exercícios filtrados");
        }
    }
}
