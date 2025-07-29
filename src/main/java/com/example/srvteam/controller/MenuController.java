package com.example.srvteam.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.srvteam.dto.request.MenuRequest;
import com.example.srvteam.dto.response.MenuResponse;
import com.example.srvteam.model.Menu;
import com.example.srvteam.service.MenuService;

import jakarta.validation.Valid;

/**
 * Controller para Menu
 * Endpoints para operações CRUD de Menu
 * Todos os endpoints são protegidos por autenticação JWT
 */
@RestController
@RequestMapping("/v1/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    /**
     * Converter Menu para MenuResponse
     */
    private MenuResponse toResponse(Menu menu) {
        String dsSistema = null;
        if (menu.getSistema() != null) {
            dsSistema = menu.getSistema().getDsSistema();
        }
        
        return new MenuResponse(
                menu.getCdMenu(),
                menu.getDsMenu(),
                menu.getCdSistema(),
                dsSistema
        );
    }

    /**
     * Converter MenuRequest para Menu
     */
    private Menu fromRequest(MenuRequest request) {
        Menu menu = new Menu();
        menu.setDsMenu(request.getDsMenu());
        menu.setCdSistema(request.getCdSistema());
        return menu;
    }

    /**
     * Converter lista de Menu para lista de MenuResponse
     */
    private List<MenuResponse> toResponseList(List<Menu> menus) {
        return menus.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * POST /v1/menu - Inserir novo menu
     * 
     * @param menuRequest Dados do menu a ser criado
     * @return ResponseEntity<MenuResponse>
     */
    @PostMapping
    public ResponseEntity<?> insMenu(@Valid @RequestBody MenuRequest menuRequest) {
        try {
            logger.info("Request para inserir novo menu: {}", menuRequest.getDsMenu());
            
            Menu menu = fromRequest(menuRequest);
            Menu novoMenu = menuService.insMenu(menu);
            
            logger.info("Menu criado com sucesso. ID: {}", novoMenu.getCdMenu());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(novoMenu));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao inserir menu: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao inserir menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * PUT /v1/menu/{cdMenu} - Atualizar menu existente
     * 
     * @param cdMenu Código do menu a ser atualizado
     * @param menuRequest Dados atualizados do menu
     * @return ResponseEntity<MenuResponse>
     */
    @PutMapping("/{cdMenu}")
    public ResponseEntity<?> updMenu(@PathVariable Integer cdMenu, 
                                    @Valid @RequestBody MenuRequest menuRequest) {
        try {
            logger.info("Request para atualizar menu ID: {}", cdMenu);
            
            Menu menu = fromRequest(menuRequest);
            menu.setCdMenu(cdMenu);
            Menu menuAtualizado = menuService.updMenu(menu);
            
            logger.info("Menu atualizado com sucesso. ID: {}", cdMenu);
            return ResponseEntity.ok(toResponse(menuAtualizado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar menu: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao atualizar menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * DELETE /v1/menu/{cdMenu} - Deletar menu
     * 
     * @param cdMenu Código do menu a ser deletado
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{cdMenu}")
    public ResponseEntity<?> delMenu(@PathVariable Integer cdMenu) {
        try {
            logger.info("Request para deletar menu ID: {}", cdMenu);
            
            menuService.delMenu(cdMenu);
            
            logger.info("Menu deletado com sucesso. ID: {}", cdMenu);
            return ResponseEntity.ok("Menu deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao deletar menu: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao deletar menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/menu/{cdMenu} - Obter menu por código
     * 
     * @param cdMenu Código do menu
     * @return ResponseEntity<MenuResponse>
     */
    @GetMapping("/{cdMenu}")
    public ResponseEntity<?> getMenu(@PathVariable Integer cdMenu) {
        try {
            logger.info("Request para buscar menu ID: {}", cdMenu);
            
            Menu menu = menuService.getMenu(cdMenu);
            
            logger.info("Menu encontrado: {}", menu.getDsMenu());
            return ResponseEntity.ok(toResponse(menu));
            
        } catch (IllegalArgumentException e) {
            logger.error("Menu não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao buscar menu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }

    /**
     * GET /v1/menu/sistema/{cdSistema} - Listar menus de um sistema
     * 
     * @param cdSistema Código do sistema
     * @return ResponseEntity<List<MenuResponse>>
     */
    @GetMapping("/sistema/{cdSistema}")
    public ResponseEntity<?> listMenuBySistema(@PathVariable Integer cdSistema) {
        try {
            logger.info("Request para listar menus do sistema ID: {}", cdSistema);
            
            List<Menu> menus = menuService.listMenuBySistema(cdSistema);
            
            logger.info("Encontrados {} menus para o sistema ID: {}", menus.size(), cdSistema);
            return ResponseEntity.ok(toResponseList(menus));
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao listar menus: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Erro interno ao listar menus", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor");
        }
    }
}
