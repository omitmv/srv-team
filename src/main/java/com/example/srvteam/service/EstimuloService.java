package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Estimulo;
import com.example.srvteam.repository.EstimuloRepository;

/**
 * Service para Estimulo
 * Contém a lógica de negócio para operações com Estimulo
 */
@Service
@Transactional
public class EstimuloService {

    private static final Logger logger = LoggerFactory.getLogger(EstimuloService.class);

    @Autowired
    private EstimuloRepository estimuloRepository;

    /**
     * Inserir novo estímulo
     * 
     * @param estimulo Dados do estímulo
     * @return Estimulo criado
     * @throws IllegalArgumentException se já existe estímulo com a mesma descrição
     */
    public Estimulo insEstimulo(Estimulo estimulo) {
        logger.info("Iniciando inserção de novo estímulo: {}", estimulo.getDsEstimulo());
        
        // Verificar se já existe estímulo com essa descrição
        if (estimuloRepository.existsByDsEstimulo(estimulo.getDsEstimulo())) {
            logger.error("Tentativa de inserir estímulo com descrição já existente: {}", estimulo.getDsEstimulo());
            throw new IllegalArgumentException("Já existe um estímulo com a descrição: " + estimulo.getDsEstimulo());
        }
        
        try {
            Estimulo estimuloSalvo = estimuloRepository.save(estimulo);
            logger.info("Estímulo inserido com sucesso. ID: {}", estimuloSalvo.getCdEstimulo());
            return estimuloSalvo;
        } catch (Exception e) {
            logger.error("Erro ao inserir estímulo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao inserir estímulo");
        }
    }

    /**
     * Atualizar estímulo existente
     * 
     * @param estimulo Dados do estímulo para atualização
     * @return Estimulo atualizado
     * @throws IllegalArgumentException se estímulo não existe ou descrição já existe para outro estímulo
     */
    public Estimulo updEstimulo(Estimulo estimulo) {
        logger.info("Iniciando atualização do estímulo ID: {}", estimulo.getCdEstimulo());
        
        // Verificar se o estímulo existe
        Optional<Estimulo> estimuloExistente = estimuloRepository.findById(estimulo.getCdEstimulo());
        if (estimuloExistente.isEmpty()) {
            logger.error("Tentativa de atualizar estímulo inexistente. ID: {}", estimulo.getCdEstimulo());
            throw new IllegalArgumentException("Estímulo não encontrado com o código: " + estimulo.getCdEstimulo());
        }
        
        // Verificar se já existe outro estímulo com essa descrição
        if (estimuloRepository.existsByDsEstimuloAndCdEstimuloNot(
                estimulo.getDsEstimulo(), estimulo.getCdEstimulo())) {
            logger.error("Tentativa de atualizar estímulo com descrição já existente: {}", estimulo.getDsEstimulo());
            throw new IllegalArgumentException("Já existe outro estímulo com a descrição: " + estimulo.getDsEstimulo());
        }
        
        try {
            Estimulo estimuloAtualizado = estimuloRepository.save(estimulo);
            logger.info("Estímulo atualizado com sucesso. ID: {}", estimuloAtualizado.getCdEstimulo());
            return estimuloAtualizado;
        } catch (Exception e) {
            logger.error("Erro ao atualizar estímulo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar estímulo");
        }
    }

    /**
     * Deletar estímulo
     * 
     * @param cdEstimulo Código do estímulo
     * @throws IllegalArgumentException se estímulo não existe
     */
    public void delEstimulo(Integer cdEstimulo) {
        logger.info("Iniciando exclusão do estímulo ID: {}", cdEstimulo);
        
        // Verificar se o estímulo existe
        if (!estimuloRepository.existsById(cdEstimulo)) {
            logger.error("Tentativa de deletar estímulo inexistente. ID: {}", cdEstimulo);
            throw new IllegalArgumentException("Estímulo não encontrado com o código: " + cdEstimulo);
        }
        
        try {
            estimuloRepository.deleteById(cdEstimulo);
            logger.info("Estímulo deletado com sucesso. ID: {}", cdEstimulo);
        } catch (Exception e) {
            logger.error("Erro ao deletar estímulo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar estímulo");
        }
    }

    /**
     * Obter estímulo por código
     * 
     * @param cdEstimulo Código do estímulo
     * @return Estimulo encontrado
     * @throws IllegalArgumentException se estímulo não existe
     */
    @Transactional(readOnly = true)
    public Estimulo getEstimulo(Integer cdEstimulo) {
        logger.info("Buscando estímulo por ID: {}", cdEstimulo);
        
        Optional<Estimulo> estimulo = estimuloRepository.findById(cdEstimulo);
        if (estimulo.isEmpty()) {
            logger.error("Estímulo não encontrado. ID: {}", cdEstimulo);
            throw new IllegalArgumentException("Estímulo não encontrado com o código: " + cdEstimulo);
        }
        
        logger.info("Estímulo encontrado: {}", estimulo.get().getDsEstimulo());
        return estimulo.get();
    }

    /**
     * Listar todos os estímulos
     * 
     * @return Lista de estímulos ordenada por descrição
     * 
     * @apiNote Este método atualmente lista todos os estímulos. 
     *          O método listEstimulo(Integer cdTreino) será implementado 
     *          quando os relacionamentos com Treino forem estabelecidos.
     */
    @Transactional(readOnly = true)
    public List<Estimulo> listEstimulo() {
        logger.info("Listando todos os estímulos");
        
        try {
            List<Estimulo> estimulos = estimuloRepository.findAllOrderByDsEstimulo();
            logger.info("Encontrados {} estímulos", estimulos.size());
            return estimulos;
        } catch (Exception e) {
            logger.error("Erro ao listar estímulos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar estímulos");
        }
    }
    
    /**
     * Listar todos os estímulos de um treino específico
     * 
     * @param cdTreino Código do treino
     * @return Lista de estímulos do treino
     * 
     * @apiNote Este método será implementado quando os relacionamentos 
     *          entre Estimulo e Treino forem estabelecidos através da 
     *          tabela de relacionamento TreinoEstimulo.
     */
    @Transactional(readOnly = true)
    public List<Estimulo> listEstimuloByTreino(Integer cdTreino) {
        logger.info("Listando estímulos do treino ID: {}", cdTreino);
        
        // TODO: Implementar quando a tabela TreinoEstimulo for criada
        // Este método deverá fazer um JOIN com a tabela tbTreinoEstimulo
        throw new UnsupportedOperationException(
            "Método será implementado quando os relacionamentos com Treino forem estabelecidos"
        );
    }
}
