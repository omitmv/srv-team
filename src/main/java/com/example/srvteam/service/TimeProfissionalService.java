package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Time;
import com.example.srvteam.model.TimeProfissional;
import com.example.srvteam.model.TimeProfissionalId;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.repository.TimeProfissionalRepository;
import com.example.srvteam.repository.TimeRepository;
import com.example.srvteam.repository.UsuarioRepository;

@Service
public class TimeProfissionalService {

    @Autowired
    private TimeProfissionalRepository timeProfissionalRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inserir novo profissional ao time
     * 
     * @param timeProfissional Dados da associação
     * @return TimeProfissional criado
     */
    @Transactional
    public TimeProfissional insTimeProfissional(TimeProfissional timeProfissional) {
        // Verificar se o time existe
        Optional<Time> time = timeRepository.findById(timeProfissional.getCdTime());
        if (time.isEmpty()) {
            throw new IllegalArgumentException("Time não encontrado com ID: " + timeProfissional.getCdTime());
        }

        // Verificar se o profissional existe e está ativo
        Optional<Usuario> profissional = usuarioRepository.findById(timeProfissional.getCdProfissional());
        if (profissional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado com ID: " + timeProfissional.getCdProfissional());
        }

        if (!profissional.get().getFlAtivo()) {
            throw new IllegalArgumentException("Profissional está inativo");
        }

        // Verificar se o usuário tem perfil adequado (não pode ser atleta)
        if (profissional.get().getCdTpAcesso() != null && profissional.get().getCdTpAcesso() == 6) {
            throw new IllegalArgumentException("Atletas não podem fazer parte da equipe técnica de times");
        }

        // Verificar se a associação já existe
        if (timeProfissionalRepository.existsByCdTimeAndCdProfissional(
                timeProfissional.getCdTime(), timeProfissional.getCdProfissional())) {
            throw new IllegalArgumentException("Profissional já está associado a este time");
        }

        return timeProfissionalRepository.save(timeProfissional);
    }

    /**
     * Deletar profissional do time
     * 
     * @param cdTime ID do time
     * @param cdProfissional ID do profissional
     */
    @Transactional
    public void delTimeProfissional(Integer cdTime, Integer cdProfissional) {
        TimeProfissionalId id = new TimeProfissionalId(cdTime, cdProfissional);
        TimeProfissional timeProfissional = timeProfissionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Associação não encontrada entre time " + cdTime + " e profissional " + cdProfissional));
        
        timeProfissionalRepository.delete(timeProfissional);
    }

    /**
     * Obter associação entre time e profissional
     * 
     * @param cdTime ID do time
     * @param cdProfissional ID do profissional
     * @return TimeProfissional encontrado
     */
    public Optional<TimeProfissional> getTimeProfissional(Integer cdTime, Integer cdProfissional) {
        return timeProfissionalRepository.findByIdWithFullDetails(cdTime, cdProfissional);
    }

    /**
     * Listar todos os profissionais de um time
     * 
     * @param cdTime ID do time
     * @return Lista de profissionais do time
     */
    public List<TimeProfissional> listTimeProfissionalByTime(Integer cdTime) {
        // Verificar se o time existe
        if (!timeRepository.existsById(cdTime)) {
            throw new IllegalArgumentException("Time não encontrado com ID: " + cdTime);
        }

        return timeProfissionalRepository.findByTimeWithProfissionalDetails(cdTime);
    }

    /**
     * Listar todos os times de um profissional
     * 
     * @param cdProfissional ID do profissional
     * @return Lista de times do profissional
     */
    public List<TimeProfissional> listTimeProfissionalByProfissional(Integer cdProfissional) {
        // Verificar se o profissional existe
        if (!usuarioRepository.existsById(cdProfissional)) {
            throw new IllegalArgumentException("Profissional não encontrado com ID: " + cdProfissional);
        }

        return timeProfissionalRepository.findByProfissionalWithTimeDetails(cdProfissional);
    }

    /**
     * Contar profissionais em um time
     * 
     * @param cdTime ID do time
     * @return Número de profissionais no time
     */
    public long countProfissionaisNoTime(Integer cdTime) {
        return timeProfissionalRepository.countByCdTime(cdTime);
    }

    /**
     * Contar times de um profissional
     * 
     * @param cdProfissional ID do profissional
     * @return Número de times do profissional
     */
    public long countTimesDosProfissional(Integer cdProfissional) {
        return timeProfissionalRepository.countByCdProfissional(cdProfissional);
    }

    /**
     * Verificar se profissional está no time
     * 
     * @param cdTime ID do time
     * @param cdProfissional ID do profissional
     * @return true se estiver associado, false caso contrário
     */
    public boolean isProfissionalNoTime(Integer cdTime, Integer cdProfissional) {
        return timeProfissionalRepository.existsByCdTimeAndCdProfissional(cdTime, cdProfissional);
    }

    /**
     * Remover todos os profissionais de um time
     * 
     * @param cdTime ID do time
     */
    @Transactional
    public void removeAllProfissionaisDoTime(Integer cdTime) {
        timeProfissionalRepository.deleteByCdTime(cdTime);
    }
}
