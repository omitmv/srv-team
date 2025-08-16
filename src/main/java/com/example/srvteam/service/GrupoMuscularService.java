package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.GrupoMuscular;
import com.example.srvteam.repository.GrupoMuscularRepository;

/**
 * Service para GrupoMuscular
 * Contém a lógica de negócio para operações com GrupoMuscular
 */
@Service
@Transactional
public class GrupoMuscularService {

    private static final Logger logger = LoggerFactory.getLogger(GrupoMuscularService.class);

    @Autowired
    private GrupoMuscularRepository grupoMuscularRepository;

    /**
     * Inserir novo grupo muscular
     * 
     * @param grupoMuscular Dados do grupo muscular
     * @return GrupoMuscular criado
     * @throws IllegalArgumentException se já existe grupo muscular com a mesma descrição
     */
    public GrupoMuscular insGrupoMuscular(GrupoMuscular grupoMuscular) {
        logger.info("Iniciando inserção de novo grupo muscular: {}", grupoMuscular.getDsGrupoMuscular());
        
        // Verificar se já existe grupo muscular com essa descrição
        if (grupoMuscularRepository.existsByDsGrupoMuscular(grupoMuscular.getDsGrupoMuscular())) {
            logger.error("Tentativa de inserir grupo muscular com descrição já existente: {}", grupoMuscular.getDsGrupoMuscular());
            throw new IllegalArgumentException("Já existe um grupo muscular com a descrição: " + grupoMuscular.getDsGrupoMuscular());
        }
        
        try {
            GrupoMuscular grupoMuscularSalvo = grupoMuscularRepository.save(grupoMuscular);
            logger.info("Grupo muscular inserido com sucesso. ID: {}", grupoMuscularSalvo.getCdGrupoMuscular());
            return grupoMuscularSalvo;
        } catch (Exception e) {
            logger.error("Erro ao inserir grupo muscular: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao inserir grupo muscular");
        }
    }

    /**
     * Atualizar grupo muscular existente
     * 
     * @param grupoMuscular Dados do grupo muscular para atualização
     * @return GrupoMuscular atualizado
     * @throws IllegalArgumentException se grupo muscular não existe ou descrição já existe para outro grupo
     */
    public GrupoMuscular updGrupoMuscular(GrupoMuscular grupoMuscular) {
        logger.info("Iniciando atualização do grupo muscular ID: {}", grupoMuscular.getCdGrupoMuscular());
        
        // Verificar se o grupo muscular existe
        Optional<GrupoMuscular> grupoMuscularExistente = grupoMuscularRepository.findById(grupoMuscular.getCdGrupoMuscular());
        if (grupoMuscularExistente.isEmpty()) {
            logger.error("Tentativa de atualizar grupo muscular inexistente. ID: {}", grupoMuscular.getCdGrupoMuscular());
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + grupoMuscular.getCdGrupoMuscular());
        }
        
        // Verificar se já existe outro grupo muscular com essa descrição
        if (grupoMuscularRepository.existsByDsGrupoMuscularAndCdGrupoMuscularNot(
                grupoMuscular.getDsGrupoMuscular(), grupoMuscular.getCdGrupoMuscular())) {
            logger.error("Tentativa de atualizar grupo muscular com descrição já existente: {}", grupoMuscular.getDsGrupoMuscular());
            throw new IllegalArgumentException("Já existe outro grupo muscular com a descrição: " + grupoMuscular.getDsGrupoMuscular());
        }
        
        try {
            GrupoMuscular grupoMuscularAtualizado = grupoMuscularRepository.save(grupoMuscular);
            logger.info("Grupo muscular atualizado com sucesso. ID: {}", grupoMuscularAtualizado.getCdGrupoMuscular());
            return grupoMuscularAtualizado;
        } catch (Exception e) {
            logger.error("Erro ao atualizar grupo muscular: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar grupo muscular");
        }
    }

    /**
     * Deletar grupo muscular
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @throws IllegalArgumentException se grupo muscular não existe
     */
    public void delGrupoMuscular(Integer cdGrupoMuscular) {
        logger.info("Iniciando exclusão do grupo muscular ID: {}", cdGrupoMuscular);
        
        // Verificar se o grupo muscular existe
        if (!grupoMuscularRepository.existsById(cdGrupoMuscular)) {
            logger.error("Tentativa de deletar grupo muscular inexistente. ID: {}", cdGrupoMuscular);
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + cdGrupoMuscular);
        }
        
        try {
            grupoMuscularRepository.deleteById(cdGrupoMuscular);
            logger.info("Grupo muscular deletado com sucesso. ID: {}", cdGrupoMuscular);
        } catch (Exception e) {
            logger.error("Erro ao deletar grupo muscular: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar grupo muscular");
        }
    }

    /**
     * Obter grupo muscular por código
     * 
     * @param cdGrupoMuscular Código do grupo muscular
     * @return GrupoMuscular encontrado
     * @throws IllegalArgumentException se grupo muscular não existe
     */
    @Transactional(readOnly = true)
    public GrupoMuscular getGrupoMuscular(Integer cdGrupoMuscular) {
        logger.info("Buscando grupo muscular por ID: {}", cdGrupoMuscular);
        
        Optional<GrupoMuscular> grupoMuscular = grupoMuscularRepository.findById(cdGrupoMuscular);
        if (grupoMuscular.isEmpty()) {
            logger.error("Grupo muscular não encontrado. ID: {}", cdGrupoMuscular);
            throw new IllegalArgumentException("Grupo muscular não encontrado com o código: " + cdGrupoMuscular);
        }
        
        logger.info("Grupo muscular encontrado: {}", grupoMuscular.get().getDsGrupoMuscular());
        return grupoMuscular.get();
    }

    /**
     * Listar todos os grupos musculares
     * 
     * @return Lista de grupos musculares ordenada por descrição
     */
    @Transactional(readOnly = true)
    public List<GrupoMuscular> listGrupoMuscular() {
        logger.info("Listando todos os grupos musculares");
        
        try {
            List<GrupoMuscular> gruposMusculares = grupoMuscularRepository.findAllOrderByDsGrupoMuscular();
            logger.info("Encontrados {} grupos musculares", gruposMusculares.size());
            return gruposMusculares;
        } catch (Exception e) {
            logger.error("Erro ao listar grupos musculares: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar grupos musculares");
        }
    }
}
