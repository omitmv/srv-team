package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Treino;
import com.example.srvteam.repository.TreinoRepository;
import com.example.srvteam.repository.UsuarioRepository;

/**
 * Service para Treino
 * Contém a lógica de negócio para operações com Treino
 */
@Service
@Transactional
public class TreinoService {

    private static final Logger logger = LoggerFactory.getLogger(TreinoService.class);

    @Autowired
    private TreinoRepository treinoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inserir novo treino
     * 
     * @param treino Dados do treino
     * @return Treino criado
     * @throws IllegalArgumentException se já existe treino com a mesma descrição ou usuários não existem
     */
    public Treino insTreino(Treino treino) {
        logger.info("Iniciando inserção de novo treino: {}", treino.getDsTreino());
        
        // Verificar se já existe treino com essa descrição
        if (treinoRepository.existsByDsTreino(treino.getDsTreino())) {
            logger.error("Tentativa de inserir treino com descrição já existente: {}", treino.getDsTreino());
            throw new IllegalArgumentException("Já existe um treino com a descrição: " + treino.getDsTreino());
        }
        
        // Verificar se o profissional existe
        if (!usuarioRepository.existsById(treino.getCdProfissional())) {
            logger.error("Tentativa de inserir treino com profissional inexistente: {}", treino.getCdProfissional());
            throw new IllegalArgumentException("Profissional não encontrado com o código: " + treino.getCdProfissional());
        }
        
        // Verificar se o atleta existe
        if (!usuarioRepository.existsById(treino.getCdAtleta())) {
            logger.error("Tentativa de inserir treino com atleta inexistente: {}", treino.getCdAtleta());
            throw new IllegalArgumentException("Atleta não encontrado com o código: " + treino.getCdAtleta());
        }
        
        // Validar datas
        if (treino.getDtInicio().isAfter(treino.getDtFinal())) {
            logger.error("Data de início não pode ser posterior à data final");
            throw new IllegalArgumentException("Data de início não pode ser posterior à data final");
        }
        
        Treino treinoSalvo = treinoRepository.save(treino);
        logger.info("Treino inserido com sucesso: {}", treinoSalvo.getCdTreino());
        
        return treinoSalvo;
    }

    /**
     * Atualizar treino existente
     * 
     * @param treino Dados do treino para atualização
     * @return Treino atualizado
     * @throws IllegalArgumentException se treino não existe, usuários não existem ou descrição já existe para outro treino
     */
    public Treino updTreino(Treino treino) {
        logger.info("Iniciando atualização do treino: {}", treino.getCdTreino());
        
        // Verificar se o treino existe
        if (!treinoRepository.existsById(treino.getCdTreino())) {
            logger.error("Tentativa de atualizar treino inexistente: {}", treino.getCdTreino());
            throw new IllegalArgumentException("Treino não encontrado com o código: " + treino.getCdTreino());
        }
        
        // Verificar se já existe outro treino com essa descrição
        if (treinoRepository.existsByDsTreinoAndCdTreinoNot(treino.getDsTreino(), treino.getCdTreino())) {
            logger.error("Tentativa de atualizar treino para descrição já existente: {}", treino.getDsTreino());
            throw new IllegalArgumentException("Já existe outro treino com a descrição: " + treino.getDsTreino());
        }
        
        // Verificar se o profissional existe
        if (!usuarioRepository.existsById(treino.getCdProfissional())) {
            logger.error("Tentativa de atualizar treino com profissional inexistente: {}", treino.getCdProfissional());
            throw new IllegalArgumentException("Profissional não encontrado com o código: " + treino.getCdProfissional());
        }
        
        // Verificar se o atleta existe
        if (!usuarioRepository.existsById(treino.getCdAtleta())) {
            logger.error("Tentativa de atualizar treino com atleta inexistente: {}", treino.getCdAtleta());
            throw new IllegalArgumentException("Atleta não encontrado com o código: " + treino.getCdAtleta());
        }
        
        // Validar datas
        if (treino.getDtInicio().isAfter(treino.getDtFinal())) {
            logger.error("Data de início não pode ser posterior à data final");
            throw new IllegalArgumentException("Data de início não pode ser posterior à data final");
        }
        
        Treino treinoAtualizado = treinoRepository.save(treino);
        logger.info("Treino atualizado com sucesso: {}", treinoAtualizado.getCdTreino());
        
        return treinoAtualizado;
    }

    /**
     * Deletar treino
     * 
     * @param cdTreino Código do treino
     * @throws IllegalArgumentException se treino não existe
     */
    public void delTreino(Integer cdTreino) {
        logger.info("Iniciando deleção do treino: {}", cdTreino);
        
        if (!treinoRepository.existsById(cdTreino)) {
            logger.error("Tentativa de deletar treino inexistente: {}", cdTreino);
            throw new IllegalArgumentException("Treino não encontrado com o código: " + cdTreino);
        }
        
        treinoRepository.deleteById(cdTreino);
        logger.info("Treino deletado com sucesso: {}", cdTreino);
    }

    /**
     * Obter treino por código
     * 
     * @param cdTreino Código do treino
     * @return Optional<Treino>
     */
    public Optional<Treino> getTreino(Integer cdTreino) {
        logger.info("Buscando treino: {}", cdTreino);
        return treinoRepository.findById(cdTreino);
    }

    /**
     * Listar treinos por atleta
     * 
     * @param cdAtleta Código do atleta
     * @return List<Treino>
     */
    public List<Treino> listTreinoByAtleta(Integer cdAtleta) {
        logger.info("Listando treinos do atleta: {}", cdAtleta);
        return treinoRepository.findByCdAtletaOrderByDtCadastroDesc(cdAtleta);
    }

    /**
     * Listar treinos por profissional
     * 
     * @param cdProfissional Código do profissional
     * @return List<Treino>
     */
    public List<Treino> listTreinoByProfissional(Integer cdProfissional) {
        logger.info("Listando treinos do profissional: {}", cdProfissional);
        return treinoRepository.findByCdProfissionalOrderByDtCadastroDesc(cdProfissional);
    }

    /**
     * Listar treinos por atleta e profissional
     * 
     * @param cdAtleta Código do atleta
     * @param cdProfissional Código do profissional
     * @return List<Treino>
     */
    public List<Treino> listTreinoByAtletaAndProfissional(Integer cdAtleta, Integer cdProfissional) {
        logger.info("Listando treinos do atleta {} e profissional {}", cdAtleta, cdProfissional);
        return treinoRepository.findByCdAtletaAndCdProfissionalOrderByDtCadastroDesc(cdAtleta, cdProfissional);
    }
}
