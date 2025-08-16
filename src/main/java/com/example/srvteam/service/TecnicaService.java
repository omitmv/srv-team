package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Tecnica;
import com.example.srvteam.repository.TecnicaRepository;

/**
 * Service para Tecnica
 * Contém a lógica de negócio para operações com Tecnica
 */
@Service
@Transactional
public class TecnicaService {

    private static final Logger logger = LoggerFactory.getLogger(TecnicaService.class);

    @Autowired
    private TecnicaRepository tecnicaRepository;

    /**
     * Inserir nova técnica
     * 
     * @param tecnica Dados da técnica
     * @return Tecnica criada
     * @throws IllegalArgumentException se já existe técnica com a mesma descrição
     */
    public Tecnica insTecnica(Tecnica tecnica) {
        logger.info("Iniciando inserção de nova técnica: {}", tecnica.getDsTecnica());
        
        // Verificar se já existe técnica com essa descrição
        if (tecnicaRepository.existsByDsTecnica(tecnica.getDsTecnica())) {
            logger.error("Tentativa de inserir técnica com descrição já existente: {}", tecnica.getDsTecnica());
            throw new IllegalArgumentException("Já existe uma técnica com a descrição: " + tecnica.getDsTecnica());
        }
        
        try {
            Tecnica tecnicaSalva = tecnicaRepository.save(tecnica);
            logger.info("Técnica inserida com sucesso. ID: {}", tecnicaSalva.getCdTecnica());
            return tecnicaSalva;
        } catch (Exception e) {
            logger.error("Erro ao inserir técnica: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao inserir técnica");
        }
    }

    /**
     * Atualizar técnica existente
     * 
     * @param tecnica Dados da técnica para atualização
     * @return Tecnica atualizada
     * @throws IllegalArgumentException se técnica não existe ou descrição já existe para outra técnica
     */
    public Tecnica updTecnica(Tecnica tecnica) {
        logger.info("Iniciando atualização da técnica ID: {}", tecnica.getCdTecnica());
        
        // Verificar se a técnica existe
        Optional<Tecnica> tecnicaExistente = tecnicaRepository.findById(tecnica.getCdTecnica());
        if (tecnicaExistente.isEmpty()) {
            logger.error("Tentativa de atualizar técnica inexistente. ID: {}", tecnica.getCdTecnica());
            throw new IllegalArgumentException("Técnica não encontrada com o código: " + tecnica.getCdTecnica());
        }
        
        // Verificar se já existe outra técnica com essa descrição
        if (tecnicaRepository.existsByDsTecnicaAndCdTecnicaNot(
                tecnica.getDsTecnica(), tecnica.getCdTecnica())) {
            logger.error("Tentativa de atualizar técnica com descrição já existente: {}", tecnica.getDsTecnica());
            throw new IllegalArgumentException("Já existe outra técnica com a descrição: " + tecnica.getDsTecnica());
        }
        
        try {
            Tecnica tecnicaAtualizada = tecnicaRepository.save(tecnica);
            logger.info("Técnica atualizada com sucesso. ID: {}", tecnicaAtualizada.getCdTecnica());
            return tecnicaAtualizada;
        } catch (Exception e) {
            logger.error("Erro ao atualizar técnica: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar técnica");
        }
    }

    /**
     * Deletar técnica
     * 
     * @param cdTecnica Código da técnica
     * @throws IllegalArgumentException se técnica não existe
     */
    public void delTecnica(Integer cdTecnica) {
        logger.info("Iniciando exclusão da técnica ID: {}", cdTecnica);
        
        // Verificar se a técnica existe
        if (!tecnicaRepository.existsById(cdTecnica)) {
            logger.error("Tentativa de deletar técnica inexistente. ID: {}", cdTecnica);
            throw new IllegalArgumentException("Técnica não encontrada com o código: " + cdTecnica);
        }
        
        try {
            tecnicaRepository.deleteById(cdTecnica);
            logger.info("Técnica deletada com sucesso. ID: {}", cdTecnica);
        } catch (Exception e) {
            logger.error("Erro ao deletar técnica: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar técnica");
        }
    }

    /**
     * Obter técnica por código
     * 
     * @param cdTecnica Código da técnica
     * @return Tecnica encontrada
     * @throws IllegalArgumentException se técnica não existe
     */
    @Transactional(readOnly = true)
    public Tecnica getTecnica(Integer cdTecnica) {
        logger.info("Buscando técnica por ID: {}", cdTecnica);
        
        Optional<Tecnica> tecnica = tecnicaRepository.findById(cdTecnica);
        if (tecnica.isEmpty()) {
            logger.error("Técnica não encontrada. ID: {}", cdTecnica);
            throw new IllegalArgumentException("Técnica não encontrada com o código: " + cdTecnica);
        }
        
        logger.info("Técnica encontrada: {}", tecnica.get().getDsTecnica());
        return tecnica.get();
    }

    /**
     * Listar todas as técnicas
     * 
     * @return Lista de técnicas ordenada por descrição
     */
    @Transactional(readOnly = true)
    public List<Tecnica> listTecnica() {
        logger.info("Listando todas as técnicas");
        
        try {
            List<Tecnica> tecnicas = tecnicaRepository.findAllOrderByDsTecnica();
            logger.info("Encontradas {} técnicas", tecnicas.size());
            return tecnicas;
        } catch (Exception e) {
            logger.error("Erro ao listar técnicas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar técnicas");
        }
    }
}
