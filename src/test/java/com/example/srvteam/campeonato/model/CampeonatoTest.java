package com.example.srvteam.campeonato.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.srvteam.catalogo.model.Organizador;
import com.example.srvteam.catalogo.model.Pais;
import com.example.srvteam.catalogo.model.Subdivisao;
import com.example.srvteam.model.Usuario;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CampeonatoTest {

    @Test
    void shouldNormalizeNameAndKeepDefaultStatus() throws Exception {
        Pais pais = pais("BR", "BRA", "Brasil", true, 1);
        Organizador organizador = organizador("Federação Águia", 1, true);
        Subdivisao subdivisao = subdivisao(pais, "BR-SP", "São Paulo", true, 1);
        Usuario criador = usuario("criador");

        Campeonato campeonato = new Campeonato(
                "  CAMPÊONATO   Nacional  ",
                organizador,
                pais,
                subdivisao,
                "Ginásio Central",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 11),
                criador,
                1);

        assertEquals("campeonato nacional", campeonato.getNmCompeticaoNormalizado());
        assertEquals(CampeonatoStatus.ATIVO, campeonato.getStatus());
        assertEquals(subdivisao, campeonato.getSubdivisao());
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() throws Exception {
        Pais pais = pais("BR", "BRA", "Brasil", true, 1);
        Organizador organizador = organizador("Federação Águia", 1, true);
        Usuario criador = usuario("criador");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Campeonato(
                "Campeonato X",
                organizador,
                pais,
                null,
                "Local",
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 1),
                criador,
                1));

        assertEquals("dtFim não pode ser anterior a dtInicio", exception.getMessage());
    }

    @Test
    void shouldAllowNullSubdivision() throws Exception {
        Pais pais = pais("US", "USA", "United States", true, 2);
        Organizador organizador = organizador("US Federation", 1, true);
        Usuario criador = usuario("criador");

        Campeonato campeonato = new Campeonato(
                "US Open",
                organizador,
                pais,
                null,
                "Las Vegas",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                criador,
                1);

        assertNull(campeonato.getSubdivisao());
    }

    @Test
    void shouldRejectSubdivisionFromDifferentCountry() throws Exception {
        Pais brasil = pais("BR", "BRA", "Brasil", true, 1);
        Pais argentina = pais("AR", "ARG", "Argentina", true, 2);
        Subdivisao subdivisao = subdivisao(argentina, "AR-B", "Buenos Aires", true, 7);
        Organizador organizador = organizador("Federação Águia", 1, true);
        Usuario criador = usuario("criador");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Campeonato(
                "Sulamericano",
                organizador,
                brasil,
                subdivisao,
                "Arena Sul",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 1),
                criador,
                1));

        assertEquals("Subdivisão deve pertencer ao país informado", exception.getMessage());
    }

    @Test
    void shouldRejectInactiveCatalogs() throws Exception {
        Usuario criador = usuario("criador");

        Pais paisAtivo = pais("BR", "BRA", "Brasil", true, 1);
        Organizador organizadorInativo = organizador("Federação Águia", 1, false);
        IllegalArgumentException organizerError = assertThrows(IllegalArgumentException.class, () -> new Campeonato(
                "Copa A",
                organizadorInativo,
                paisAtivo,
                null,
                "Local",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1),
                criador,
                1));
        assertEquals("Organizador inativo não pode ser utilizado", organizerError.getMessage());

        Organizador organizadorAtivo = organizador("Federação Águia", 1, true);
        Pais paisInativo = pais("BR", "BRA", "Brasil", false, 1);
        IllegalArgumentException countryError = assertThrows(IllegalArgumentException.class, () -> new Campeonato(
                "Copa B",
                organizadorAtivo,
                paisInativo,
                null,
                "Local",
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 2),
                criador,
                1));
        assertEquals("País inativo não pode ser utilizado", countryError.getMessage());

        Pais pais = pais("BR", "BRA", "Brasil", true, 1);
        Subdivisao subdivisaoInativa = subdivisao(pais, "BR-SP", "São Paulo", false, 10);
        IllegalArgumentException subdivisionError = assertThrows(IllegalArgumentException.class, () -> new Campeonato(
                "Copa C",
                organizadorAtivo,
                pais,
                subdivisaoInativa,
                "Local",
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 3),
                criador,
                1));
        assertEquals("Subdivisão inativa não pode ser utilizada", subdivisionError.getMessage());
    }

    private Usuario usuario(String login) {
        return new Usuario(login, "senha", "Nome " + login, login + "@example.com", 1);
    }

    private Organizador organizador(String nome, Integer cdUsuarioCadastro, boolean ativo) throws Exception {
        Organizador organizador = new Organizador(nome, cdUsuarioCadastro);
        setField(organizador, "flAtivo", ativo);
        return organizador;
    }

    private Pais pais(String iso2, String iso3, String nome, boolean ativo, int cdPais) throws Exception {
        Pais pais = new Pais(iso2, iso3, nome);
        setField(pais, "flAtivo", ativo);
        setField(pais, "cdPais", cdPais);
        return pais;
    }

    private Subdivisao subdivisao(Pais pais, String codigoIso, String nome, boolean ativo, int cdSubdivisao)
            throws Exception {
        Subdivisao subdivisao = new Subdivisao(pais, codigoIso, nome);
        setField(subdivisao, "flAtivo", ativo);
        setField(subdivisao, "cdSubdivisao", cdSubdivisao);
        return subdivisao;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
