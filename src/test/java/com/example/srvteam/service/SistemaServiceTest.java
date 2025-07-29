package com.example.srvteam.service;

import com.example.srvteam.model.Sistema;
import com.example.srvteam.repository.SistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SistemaServiceTest {

    @Mock
    private SistemaRepository sistemaRepository;

    @InjectMocks
    private SistemaService sistemaService;

    private Sistema sistema;

    @BeforeEach
    void setUp() {
        sistema = new Sistema();
        sistema.setCdSistema(1);
        sistema.setDsSistema("Sistema Teste");
        sistema.setFlAtivo(true);
    }

    @Test
    void testInsSistema_Success() {
        // Arrange
        when(sistemaRepository.existsByDsSistema(anyString())).thenReturn(false);
        when(sistemaRepository.save(any(Sistema.class))).thenReturn(sistema);

        // Act
        Sistema result = sistemaService.insSistema(sistema);

        // Assert
        assertNotNull(result);
        assertEquals("Sistema Teste", result.getDsSistema());
        assertTrue(result.getFlAtivo());
        verify(sistemaRepository).existsByDsSistema("Sistema Teste");
        verify(sistemaRepository).save(sistema);
    }

    @Test
    void testInsSistema_DuplicateDescription() {
        // Arrange
        when(sistemaRepository.existsByDsSistema(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sistemaService.insSistema(sistema)
        );
        assertEquals("Já existe um sistema com essa descrição: Sistema Teste", exception.getMessage());
        verify(sistemaRepository).existsByDsSistema("Sistema Teste");
        verify(sistemaRepository, never()).save(any());
    }

    @Test
    void testGetSistema_Found() {
        // Arrange
        when(sistemaRepository.findById(1)).thenReturn(Optional.of(sistema));

        // Act
        Optional<Sistema> result = sistemaService.getSistema(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Sistema Teste", result.get().getDsSistema());
        verify(sistemaRepository).findById(1);
    }

    @Test
    void testGetSistema_NotFound() {
        // Arrange
        when(sistemaRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Optional<Sistema> result = sistemaService.getSistema(1);

        // Assert
        assertFalse(result.isPresent());
        verify(sistemaRepository).findById(1);
    }

    @Test
    void testListSistema() {
        // Arrange
        Sistema sistema2 = new Sistema("Sistema 2", true);
        sistema2.setCdSistema(2);
        List<Sistema> sistemas = Arrays.asList(sistema, sistema2);
        when(sistemaRepository.findByFlAtivoTrue()).thenReturn(sistemas);

        // Act
        List<Sistema> result = sistemaService.listSistema();

        // Assert
        assertEquals(2, result.size());
        verify(sistemaRepository).findByFlAtivoTrue();
    }

    @Test
    void testUpdSistema_Success() {
        // Arrange
        Sistema sistemaAtualizado = new Sistema("Sistema Atualizado", true);
        when(sistemaRepository.findById(1)).thenReturn(Optional.of(sistema));
        when(sistemaRepository.existsByDsSistemaAndCdSistemaNot(anyString(), anyInt())).thenReturn(false);
        when(sistemaRepository.save(any(Sistema.class))).thenReturn(sistema);

        // Act
        Sistema result = sistemaService.updSistema(1, sistemaAtualizado);

        // Assert
        assertNotNull(result);
        verify(sistemaRepository).findById(1);
        verify(sistemaRepository).existsByDsSistemaAndCdSistemaNot("Sistema Atualizado", 1);
        verify(sistemaRepository).save(any(Sistema.class));
    }

    @Test
    void testUpdSistema_NotFound() {
        // Arrange
        Sistema sistemaAtualizado = new Sistema("Sistema Atualizado", true);
        when(sistemaRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sistemaService.updSistema(1, sistemaAtualizado)
        );
        assertEquals("Sistema não encontrado com o código: 1", exception.getMessage());
        verify(sistemaRepository).findById(1);
        verify(sistemaRepository, never()).save(any());
    }

    @Test
    void testDelSistema_Success() {
        // Arrange
        when(sistemaRepository.findById(1)).thenReturn(Optional.of(sistema));
        when(sistemaRepository.save(any(Sistema.class))).thenReturn(sistema);

        // Act
        boolean result = sistemaService.delSistema(1);

        // Assert
        assertTrue(result);
        verify(sistemaRepository).findById(1);
        verify(sistemaRepository).save(any(Sistema.class));
    }

    @Test
    void testDelSistema_NotFound() {
        // Arrange
        when(sistemaRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sistemaService.delSistema(1)
        );
        assertEquals("Sistema não encontrado com o código: 1", exception.getMessage());
        verify(sistemaRepository).findById(1);
        verify(sistemaRepository, never()).save(any());
    }

    @Test
    void testExisteSistema() {
        // Arrange
        when(sistemaRepository.existsById(1)).thenReturn(true);

        // Act
        boolean result = sistemaService.existeSistema(1);

        // Assert
        assertTrue(result);
        verify(sistemaRepository).existsById(1);
    }

    @Test
    void testIsSistemaAtivo() {
        // Arrange
        when(sistemaRepository.findActiveByCdSistema(1)).thenReturn(Optional.of(sistema));

        // Act
        boolean result = sistemaService.isSistemaAtivo(1);

        // Assert
        assertTrue(result);
        verify(sistemaRepository).findActiveByCdSistema(1);
    }
}
