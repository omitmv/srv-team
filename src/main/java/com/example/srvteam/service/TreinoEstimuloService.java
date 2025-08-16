package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.TreinoEstimulo;
import com.example.srvteam.model.TreinoEstimuloId;
import com.example.srvteam.repository.EstimuloRepository;
import com.example.srvteam.repository.TreinoEstimuloRepository;
import com.example.srvteam.repository.TreinoRepository;

/**
 * Service para TreinoEstimulo
 * Contém a lógica de negócio para operações com TreinoEstimulo
 */
@Service
@Transactional
public class TreinoEstimuloService {

    private static final Logger logger = LoggerFactory.getLogger(TreinoEstimuloService.class);

    @Autowired
    private TreinoEstimuloRepository treinoEstimuloRepository;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private EstimuloRepository estimuloRepository;

    /**
     * Inserir novo TreinoEstimulo
     * 
     * @param treinoEstimulo TreinoEstimulo a ser inserido
     * @return TreinoEstimulo inserido
     * @throws IllegalArgumentException se os dados forem inválidos ou já existir a relação
     */
    public TreinoEstimulo insTreinoEstimulo(TreinoEstimulo treinoEstimulo) {
        logger.info("Iniciando inserção de TreinoEstimulo: treino={}, estimulo={}", 
                   treinoEstimulo.getCdTreino(), treinoEstimulo.getCdEstimulo());

        // Verificar se o treino existe
        if (!treinoRepository.existsById(treinoEstimulo.getCdTreino())) {
            logger.error("Tentativa de inserir TreinoEstimulo com treino inexistente: {}", treinoEstimulo.getCdTreino());
            throw new IllegalArgumentException("Treino não encontrado com o código: " + treinoEstimulo.getCdTreino());
        }

        // Verificar se o estímulo existe
        if (!estimuloRepository.existsById(treinoEstimulo.getCdEstimulo())) {
            logger.error("Tentativa de inserir TreinoEstimulo com estímulo inexistente: {}", treinoEstimulo.getCdEstimulo());
            throw new IllegalArgumentException("Estímulo não encontrado com o código: " + treinoEstimulo.getCdEstimulo());
        }

        // Verificar se já existe a relação
        if (treinoEstimuloRepository.existsByCdTreinoAndCdEstimulo(treinoEstimulo.getCdTreino(), treinoEstimulo.getCdEstimulo())) {
            logger.error("Tentativa de inserir TreinoEstimulo já existente: treino={}, estimulo={}", 
                        treinoEstimulo.getCdTreino(), treinoEstimulo.getCdEstimulo());
            throw new IllegalArgumentException("Já existe relação entre o treino " + treinoEstimulo.getCdTreino() + 
                                             " e o estímulo " + treinoEstimulo.getCdEstimulo());
        }

        TreinoEstimulo treinoEstimuloSalvo = treinoEstimuloRepository.save(treinoEstimulo);
        logger.info("TreinoEstimulo inserido com sucesso: treino={}, estimulo={}", 
                   treinoEstimuloSalvo.getCdTreino(), treinoEstimuloSalvo.getCdEstimulo());

        return treinoEstimuloSalvo;
    }

    /**
     * Deletar TreinoEstimulo
     * 
     * @param cdTreino Código do treino
     * @param cdEstimulo Código do estímulo
     * @throws IllegalArgumentException se a relação não existe
     */
    public void delTreinoEstimulo(Integer cdTreino, Integer cdEstimulo) {
        logger.info("Iniciando deleção de TreinoEstimulo: treino={}, estimulo={}", cdTreino, cdEstimulo);

        TreinoEstimuloId id = new TreinoEstimuloId(cdTreino, cdEstimulo);
        
        if (!treinoEstimuloRepository.existsById(id)) {
            logger.error("Tentativa de deletar TreinoEstimulo inexistente: treino={}, estimulo={}", cdTreino, cdEstimulo);
            throw new IllegalArgumentException("Relação não encontrada entre o treino " + cdTreino + 
                                             " e o estímulo " + cdEstimulo);
        }

        treinoEstimuloRepository.deleteById(id);
        logger.info("TreinoEstimulo deletado com sucesso: treino={}, estimulo={}", cdTreino, cdEstimulo);
    }

    /**
     * Obter TreinoEstimulo por treino e estímulo
     * 
     * @param cdTreino Código do treino
     * @param cdEstimulo Código do estímulo
     * @return Optional<TreinoEstimulo>
     */
    public Optional<TreinoEstimulo> getTreinoEstimulo(Integer cdTreino, Integer cdEstimulo) {
        logger.info("Buscando TreinoEstimulo: treino={}, estimulo={}", cdTreino, cdEstimulo);
        return treinoEstimuloRepository.findByCdTreinoAndCdEstimulo(cdTreino, cdEstimulo);
    }

    /**
     * Listar todos os estímulos de um treino específico
     * 
     * @param cdTreino Código do treino
     * @return List<TreinoEstimulo>
     */
    public List<TreinoEstimulo> listTreinoEstimuloByTreino(Integer cdTreino) {
        logger.info("Listando estímulos do treino: {}", cdTreino);
        return treinoEstimuloRepository.findByCdTreino(cdTreino);
    }

    /**
     * Listar todos os treinos de um estímulo específico
     * 
     * @param cdEstimulo Código do estímulo
     * @return List<TreinoEstimulo>
     */
    public List<TreinoEstimulo> listTreinoEstimuloByEstimulo(Integer cdEstimulo) {
        logger.info("Listando treinos do estímulo: {}", cdEstimulo);
        return treinoEstimuloRepository.findByCdEstimulo(cdEstimulo);
    }
}
