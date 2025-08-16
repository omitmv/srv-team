package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Sistema;
import com.example.srvteam.repository.SistemaRepository;

/**
 * Service para Sistema
 * Contém a lógica de negócio para operações com Sistema
 */
@Service
@Transactional
public class SistemaService {

    private static final Logger logger = LoggerFactory.getLogger(SistemaService.class);

    @Autowired
    private SistemaRepository sistemaRepository;

    /**
     * Inserir novo sistema
     * 
     * @param sistema Dados do sistema
     * @return Sistema criado
     * @throws IllegalArgumentException se já existe sistema com a mesma descrição
     */
    public Sistema insSistema(Sistema sistema) {
        logger.info("Iniciando inserção de novo sistema: {}", sistema.getDsSistema());
        
        // Verificar se já existe sistema com essa descrição
        if (sistemaRepository.existsByDsSistema(sistema.getDsSistema())) {
            logger.error("Tentativa de inserir sistema com descrição já existente: {}", sistema.getDsSistema());
            throw new IllegalArgumentException("Já existe um sistema com essa descrição: " + sistema.getDsSistema());
        }

        // Definir valores padrão se não informados
        if (sistema.getFlAtivo() == null) {
            sistema.setFlAtivo(true);
        }

        Sistema sistemasSalvo = sistemaRepository.save(sistema);
        logger.info("Sistema inserido com sucesso. ID: {}", sistemasSalvo.getCdSistema());
        
        return sistemasSalvo;
    }

    /**
     * Atualizar sistema existente
     * 
     * @param cdSistema Código do sistema a ser atualizado
     * @param sistemaAtualizado Dados atualizados do sistema
     * @return Sistema atualizado
     * @throws IllegalArgumentException se sistema não existe ou descrição já está em uso
     */
    public Sistema updSistema(Integer cdSistema, Sistema sistemaAtualizado) {
        logger.info("Iniciando atualização do sistema ID: {}", cdSistema);
        
        // Verificar se o sistema existe
        Optional<Sistema> sistemaExistente = sistemaRepository.findById(cdSistema);
        if (sistemaExistente.isEmpty()) {
            logger.error("Sistema não encontrado para atualização. ID: {}", cdSistema);
            throw new IllegalArgumentException("Sistema não encontrado com o código: " + cdSistema);
        }

        // Verificar se a nova descrição já está sendo usada por outro sistema
        if (sistemaRepository.existsByDsSistemaAndCdSistemaNot(
                sistemaAtualizado.getDsSistema(), cdSistema)) {
            logger.error("Tentativa de atualizar sistema com descrição já existente: {}", 
                    sistemaAtualizado.getDsSistema());
            throw new IllegalArgumentException("Já existe um sistema com essa descrição: " + 
                    sistemaAtualizado.getDsSistema());
        }

        // Atualizar os campos
        Sistema sistema = sistemaExistente.get();
        sistema.setDsSistema(sistemaAtualizado.getDsSistema());
        
        // Só atualizar flAtivo se foi informado
        if (sistemaAtualizado.getFlAtivo() != null) {
            sistema.setFlAtivo(sistemaAtualizado.getFlAtivo());
        }

        Sistema sistemasSalvo = sistemaRepository.save(sistema);
        logger.info("Sistema atualizado com sucesso. ID: {}", sistemasSalvo.getCdSistema());
        
        return sistemasSalvo;
    }

    /**
     * Deletar sistema (soft delete - marcar como inativo)
     * 
     * @param cdSistema Código do sistema a ser deletado
     * @return true se deletado com sucesso
     * @throws IllegalArgumentException se sistema não existe
     */
    public boolean delSistema(Integer cdSistema) {
        logger.info("Iniciando deleção do sistema ID: {}", cdSistema);
        
        Optional<Sistema> sistema = sistemaRepository.findById(cdSistema);
        if (sistema.isEmpty()) {
            logger.error("Sistema não encontrado para deleção. ID: {}", cdSistema);
            throw new IllegalArgumentException("Sistema não encontrado com o código: " + cdSistema);
        }

        // Soft delete - marcar como inativo
        Sistema sistemaParaDeletar = sistema.get();
        sistemaParaDeletar.setFlAtivo(false);
        sistemaRepository.save(sistemaParaDeletar);
        
        logger.info("Sistema deletado (marcado como inativo) com sucesso. ID: {}", cdSistema);
        return true;
    }

    /**
     * Obter sistema pelo código
     * 
     * @param cdSistema Código do sistema
     * @return Optional<Sistema>
     */
    @Transactional(readOnly = true)
    public Optional<Sistema> getSistema(Integer cdSistema) {
        logger.debug("Buscando sistema por ID: {}", cdSistema);
        return sistemaRepository.findById(cdSistema);
    }

    /**
     * Listar todos os sistemas ativos
     * 
     * @return Lista de sistemas ativos
     */
    @Transactional(readOnly = true)
    public List<Sistema> listSistema() {
        logger.debug("Listando todos os sistemas ativos");
        return sistemaRepository.findByFlAtivoTrue();
    }

    /**
     * Listar todos os sistemas (ativos e inativos)
     * 
     * @return Lista de todos os sistemas
     */
    @Transactional(readOnly = true)
    public List<Sistema> listTodosSistemas() {
        logger.debug("Listando todos os sistemas");
        return sistemaRepository.findAll();
    }

    /**
     * Verificar se sistema existe
     * 
     * @param cdSistema Código do sistema
     * @return true se existe
     */
    @Transactional(readOnly = true)
    public boolean existeSistema(Integer cdSistema) {
        return sistemaRepository.existsById(cdSistema);
    }

    /**
     * Verificar se sistema está ativo
     * 
     * @param cdSistema Código do sistema
     * @return true se está ativo
     */
    @Transactional(readOnly = true)
    public boolean isSistemaAtivo(Integer cdSistema) {
        Optional<Sistema> sistema = sistemaRepository.findActiveByCdSistema(cdSistema);
        return sistema.isPresent();
    }
}
