package com.example.srvteam.catalogo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NomeCatalogoNormalizerTest {

    @Test
    void shouldApplyTheNormativeNormalizationPolicy() {
        assertEquals("sao paulo", NomeCatalogoNormalizer.normalize("  SÃO   PAULO "));
        assertEquals("master", NomeCatalogoNormalizer.normalize("Máster"));
        assertEquals("classe-a", NomeCatalogoNormalizer.normalize("Classe-A"));
        assertEquals("classe a", NomeCatalogoNormalizer.normalize("Classe A"));
        assertEquals("master 40+", NomeCatalogoNormalizer.normalize("Master 40+"));
    }

    @Test
    void shouldKeepPunctuationAndSymbolsDistinct() {
        assertEquals("classe-a", NomeCatalogoNormalizer.normalize("Classe-A"));
        assertEquals("classe a", NomeCatalogoNormalizer.normalize("Classe A"));
        assertEquals("master 40+", NomeCatalogoNormalizer.normalize("Master 40+"));
        assertEquals("master 40", NomeCatalogoNormalizer.normalize("Master 40"));
    }

    @Test
    void shouldNormalizeUnicodeWhitespaceAndNull() {
        assertEquals("sao paulo", NomeCatalogoNormalizer.normalize("\tSÃO\u00a0PAULO\n"));
        assertEquals("", NomeCatalogoNormalizer.normalize(""));
        assertEquals("", NomeCatalogoNormalizer.normalize(" \t\n "));
        assertNull(NomeCatalogoNormalizer.normalize(null));
    }
}
