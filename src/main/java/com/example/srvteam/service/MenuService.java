package com.example.srvteam.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.srvteam.model.Menu;
import com.example.srvteam.repository.MenuRepository;
import com.example.srvteam.repository.SistemaRepository;

/**
 * Service para Menu
 * Contém a lógica de negócio para operações com Menu
 */
@Service
@Transactional
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private SistemaRepository sistemaRepository;

    /**
     * Inserir novo menu
     * 
     * @param menu Dados do menu
     * @return Menu criado
     * @throws IllegalArgumentException se já existe menu com a mesma descrição ou sistema não existe
     */
    public Menu insMenu(Menu menu) {
        logger.info("Iniciando inserção de novo menu: {}", menu.getDsMenu());
        
        // Verificar se o sistema existe
        if (!sistemaRepository.existsById(menu.getCdSistema())) {
            logger.error("Tentativa de inserir menu com sistema inexistente: {}", menu.getCdSistema());
            throw new IllegalArgumentException("Sistema não encontrado com o código: " + menu.getCdSistema());
        }
        
        // Verificar se já existe menu com essa descrição
        if (menuRepository.existsByDsMenu(menu.getDsMenu())) {
            logger.error("Tentativa de inserir menu com descrição já existente: {}", menu.getDsMenu());
            throw new IllegalArgumentException("Já existe um menu com a descrição: " + menu.getDsMenu());
        }
        
        try {
            Menu menuSalvo = menuRepository.save(menu);
            logger.info("Menu inserido com sucesso. ID: {}", menuSalvo.getCdMenu());
            return menuSalvo;
        } catch (Exception e) {
            logger.error("Erro ao inserir menu: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao inserir menu");
        }
    }

    /**
     * Atualizar menu existente
     * 
     * @param menu Dados do menu para atualização
     * @return Menu atualizado
     * @throws IllegalArgumentException se menu não existe, sistema não existe ou descrição já existe para outro menu
     */
    public Menu updMenu(Menu menu) {
        logger.info("Iniciando atualização do menu ID: {}", menu.getCdMenu());
        
        // Verificar se o menu existe
        Optional<Menu> menuExistente = menuRepository.findById(menu.getCdMenu());
        if (menuExistente.isEmpty()) {
            logger.error("Tentativa de atualizar menu inexistente. ID: {}", menu.getCdMenu());
            throw new IllegalArgumentException("Menu não encontrado com o código: " + menu.getCdMenu());
        }
        
        // Verificar se o sistema existe
        if (!sistemaRepository.existsById(menu.getCdSistema())) {
            logger.error("Tentativa de atualizar menu com sistema inexistente: {}", menu.getCdSistema());
            throw new IllegalArgumentException("Sistema não encontrado com o código: " + menu.getCdSistema());
        }
        
        // Verificar se já existe outro menu com essa descrição
        if (menuRepository.existsByDsMenuAndCdMenuNot(menu.getDsMenu(), menu.getCdMenu())) {
            logger.error("Tentativa de atualizar menu com descrição já existente: {}", menu.getDsMenu());
            throw new IllegalArgumentException("Já existe outro menu com a descrição: " + menu.getDsMenu());
        }
        
        try {
            Menu menuAtualizado = menuRepository.save(menu);
            logger.info("Menu atualizado com sucesso. ID: {}", menuAtualizado.getCdMenu());
            return menuAtualizado;
        } catch (Exception e) {
            logger.error("Erro ao atualizar menu: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar menu");
        }
    }

    /**
     * Deletar menu
     * 
     * @param cdMenu Código do menu
     * @throws IllegalArgumentException se menu não existe
     */
    public void delMenu(Integer cdMenu) {
        logger.info("Iniciando exclusão do menu ID: {}", cdMenu);
        
        // Verificar se o menu existe
        if (!menuRepository.existsById(cdMenu)) {
            logger.error("Tentativa de deletar menu inexistente. ID: {}", cdMenu);
            throw new IllegalArgumentException("Menu não encontrado com o código: " + cdMenu);
        }
        
        try {
            menuRepository.deleteById(cdMenu);
            logger.info("Menu deletado com sucesso. ID: {}", cdMenu);
        } catch (Exception e) {
            logger.error("Erro ao deletar menu: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar menu");
        }
    }

    /**
     * Obter menu por código
     * 
     * @param cdMenu Código do menu
     * @return Menu encontrado
     * @throws IllegalArgumentException se menu não existe
     */
    @Transactional(readOnly = true)
    public Menu getMenu(Integer cdMenu) {
        logger.info("Buscando menu por ID: {}", cdMenu);
        
        Optional<Menu> menu = menuRepository.findById(cdMenu);
        if (menu.isEmpty()) {
            logger.error("Menu não encontrado. ID: {}", cdMenu);
            throw new IllegalArgumentException("Menu não encontrado com o código: " + cdMenu);
        }
        
        logger.info("Menu encontrado: {}", menu.get().getDsMenu());
        return menu.get();
    }

    /**
     * Listar todos os menus de um sistema específico
     * 
     * @param cdSistema Código do sistema
     * @return Lista de menus do sistema
     * @throws IllegalArgumentException se sistema não existe
     */
    @Transactional(readOnly = true)
    public List<Menu> listMenuBySistema(Integer cdSistema) {
        logger.info("Listando menus do sistema ID: {}", cdSistema);
        
        // Verificar se o sistema existe
        if (!sistemaRepository.existsById(cdSistema)) {
            logger.error("Tentativa de listar menus de sistema inexistente: {}", cdSistema);
            throw new IllegalArgumentException("Sistema não encontrado com o código: " + cdSistema);
        }
        
        try {
            List<Menu> menus = menuRepository.findByCdSistemaOrderByDsMenu(cdSistema);
            logger.info("Encontrados {} menus para o sistema ID: {}", menus.size(), cdSistema);
            return menus;
        } catch (Exception e) {
            logger.error("Erro ao listar menus do sistema: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar menus do sistema");
        }
    }
}
