package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.srvteam.model.Time;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.repository.TimeRepository;
import com.example.srvteam.repository.UsuarioRepository;

@Service
public class TimeService {

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inserir novo time
     * 
     * @param time Dados do time
     * @return Time criado
     */
    public Time insTime(Time time) {
        // Verificar se já existe time com esse nome
        if (timeRepository.existsByDsNome(time.getDsNome())) {
            throw new IllegalArgumentException("Já existe um time com esse nome: " + time.getDsNome());
        }

        // Verificar se o profissional responsável existe e está ativo
        Optional<Usuario> profissional = usuarioRepository.findById(time.getCdProfissionalResponsavel());
        if (profissional.isEmpty()) {
            throw new IllegalArgumentException("Profissional responsável não encontrado com ID: " + time.getCdProfissionalResponsavel());
        }

        if (!profissional.get().getFlAtivo()) {
            throw new IllegalArgumentException("Profissional responsável está inativo");
        }

        // Verificar se o usuário tem perfil adequado (não pode ser atleta)
        if (profissional.get().getCdTpAcesso() != null && profissional.get().getCdTpAcesso() == 6) {
            throw new IllegalArgumentException("Atletas não podem ser responsáveis por times");
        }

        return timeRepository.save(time);
    }

    /**
     * Deletar time
     * 
     * @param cdTime ID do time
     */
    public void delTime(Integer cdTime) {
        Time time = timeRepository.findById(cdTime)
                .orElseThrow(() -> new IllegalArgumentException("Time não encontrado com ID: " + cdTime));
        timeRepository.delete(time);
    }

    /**
     * Obter time por ID
     * 
     * @param cdTime ID do time
     * @return Time encontrado
     */
    public Optional<Time> getTime(Integer cdTime) {
        return timeRepository.findByIdWithProfissional(cdTime);
    }

    /**
     * Listar times por profissional responsável
     * 
     * @param cdProfissionalResponsavel ID do profissional responsável
     * @return Lista de times
     */
    public List<Time> listTime(Integer cdProfissionalResponsavel) {
        // Verificar se o profissional existe
        if (!usuarioRepository.existsById(cdProfissionalResponsavel)) {
            throw new IllegalArgumentException("Profissional não encontrado com ID: " + cdProfissionalResponsavel);
        }

        return timeRepository.findByProfissionalWithDetails(cdProfissionalResponsavel);
    }

    /**
     * Listar todos os times
     * 
     * @return Lista de todos os times
     */
    public List<Time> getAllTimes() {
        return timeRepository.findAll();
    }

    /**
     * Buscar times por nome (case insensitive)
     * 
     * @param nome Nome do time (ou parte dele)
     * @return Lista de times encontrados
     */
    public List<Time> findTimesByNome(String nome) {
        return timeRepository.findByDsNomeContainingIgnoreCase(nome);
    }

    /**
     * Verificar se time existe por nome
     * 
     * @param dsNome Nome do time
     * @return true se existe, false caso contrário
     */
    public boolean existsByNome(String dsNome) {
        return timeRepository.existsByDsNome(dsNome);
    }
}
