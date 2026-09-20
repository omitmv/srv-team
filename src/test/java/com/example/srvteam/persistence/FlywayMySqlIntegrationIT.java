package com.example.srvteam.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.srvteam.SrvTeamApplication;
import com.example.srvteam.campeonato.model.Campeonato;
import com.example.srvteam.campeonato.model.CampeonatoStatus;
import com.example.srvteam.campeonato.repository.CampeonatoRepository;
import com.example.srvteam.catalogo.model.Categoria;
import com.example.srvteam.catalogo.model.Classe;
import com.example.srvteam.catalogo.model.Organizador;
import com.example.srvteam.catalogo.model.Pais;
import com.example.srvteam.catalogo.model.Subdivisao;
import com.example.srvteam.catalogo.model.TipoClasse;
import com.example.srvteam.catalogo.repository.CategoriaRepository;
import com.example.srvteam.catalogo.repository.ClasseRepository;
import com.example.srvteam.catalogo.repository.OrganizadorRepository;
import com.example.srvteam.catalogo.repository.PaisRepository;
import com.example.srvteam.catalogo.repository.SubdivisaoRepository;
import com.example.srvteam.model.Usuario;
import com.example.srvteam.repository.UsuarioRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Testcontainers
class FlywayMySqlIntegrationIT {

  private static final String INITIAL_MIGRATION_VERSION = "20250815";
  private static final String CATALOG_MIGRATION_VERSION = "20260914";
  private static final String SUPPORT_CATALOGS_MIGRATION_VERSION = "20260915";
  private static final String ISO_SEED_MIGRATION_VERSION = "20260916";
  private static final String CAMPEONATO_CORE_MIGRATION_VERSION = "20260917";
  private static final String JWT_SECRET = "01234567890123456789012345678901";

  @Container
  static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.6");

  @BeforeEach
  void resetDatabase() throws SQLException {
    try (Connection connection = DriverManager.getConnection(
        MYSQL.getJdbcUrl(),
        MYSQL.getUsername(),
        MYSQL.getPassword());
        Statement statement = connection.createStatement()) {
      statement.execute("SET FOREIGN_KEY_CHECKS = 0");
      statement.execute("""
          DROP TABLE IF EXISTS
          flyway_schema_history,
          tbPontuacaoHist,
          tbSubdivisao,
          tbPais,
          tbOrganizador,
          tbClasse,
          tbCategoria,
          tbTimeProfissional,
          tbTreinoEstimulo,
          tbEstimuloExercicio,
          tbCompetidores,
          tbPontuacao,
          tbMenu,
          tbTreino,
          tbTime,
          tbExercicio,
          tbTecnica,
          tbEstimulo,
          tbGrupoMuscular,
          tbSistema,
          tbCompeticao,
          tbUsuario
          """);
      statement.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
  }

  @Test
  void shouldCreateSchemaAndStartApplicationOnCleanMysqlUsingFlyway() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);

      assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbUsuario");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbCompeticao");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbTimeProfissional");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbPontuacao");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbPontuacaoHist");
      assertFlywayHistoryPresent(dataSource, MYSQL.getDatabaseName(), "flyway_schema_history");
      assertMigrationVersionPresent(dataSource, INITIAL_MIGRATION_VERSION);
      assertMigrationVersionPresent(dataSource, CATALOG_MIGRATION_VERSION);
      assertMigrationVersionPresent(dataSource, SUPPORT_CATALOGS_MIGRATION_VERSION);
      assertMigrationVersionPresent(dataSource, ISO_SEED_MIGRATION_VERSION);
      assertMigrationVersionPresent(dataSource, CAMPEONATO_CORE_MIGRATION_VERSION);
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbOrganizador");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbPais");
      assertTableExists(dataSource, MYSQL.getDatabaseName(), "tbSubdivisao");
      assertDecimalPontuacaoColumnMetadata(dataSource, MYSQL.getDatabaseName(), "tbPontuacao", "pontuacao");
    }
  }

  @Test
  void shouldPersistCatalogsAndEnforceMysqlCatalogConstraints() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      CategoriaRepository categoriaRepository = context.getBean(CategoriaRepository.class);
      ClasseRepository classeRepository = context.getBean(ClasseRepository.class);
      DataSource dataSource = context.getBean(DataSource.class);
      insertUser(dataSource);

      Categoria inativa = new Categoria("Sao Paulo", 1);
      inativa.setFlAtivo(false);
      categoriaRepository.saveAndFlush(inativa);

        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("  SÃO   PAULO ", 1));
        assertEquals("sao paulo", categoria.getNmCategoriaNormalizado());
        assertEquals(2, categoriaRepository.findAll().size());
        assertEquals(1, categoriaRepository.findAll().stream().filter(categoriaItem -> Boolean.TRUE.equals(categoriaItem.getFlAtivo())).count());

      assertThrowsDataIntegrity(() -> categoriaRepository.saveAndFlush(new Categoria("Sao Paulo", 1)));

        Classe classeInativa = new Classe(categoria, "Classe-A", TipoClasse.COMUM, 1);
        classeInativa.setFlAtivo(false);
        classeRepository.saveAndFlush(classeInativa);

        Classe classe = classeRepository.saveAndFlush(new Classe(categoria, "Classe-A", TipoClasse.COMUM, 1));
      assertEquals("classe-a", classe.getNmClasseNormalizado());
      assertEquals(TipoClasse.COMUM, classe.getTipoClasse());
        assertEquals(2, classeRepository.findAll().size());
        assertEquals(1, classeRepository.findAll().stream().filter(classeItem -> Boolean.TRUE.equals(classeItem.getFlAtivo())).count());

      classeRepository.saveAndFlush(new Classe(categoria, "Classe A", TipoClasse.COMUM, 1));
      assertThrowsDataIntegrity(
          () -> classeRepository.saveAndFlush(new Classe(categoria, "CLASSE-A", TipoClasse.COMUM, 1)));

      classeRepository.saveAndFlush(new Classe(categoria, "Overall", TipoClasse.OVERALL, 1));
      assertThrowsDataIntegrity(
          () -> classeRepository.saveAndFlush(new Classe(categoria, "Overall 2", TipoClasse.OVERALL, 1)));

      assertEquals("varchar", columnType(dataSource, "tbClasse", "tipoClasse"));
      assertEquals("OVERALL", enumValue(dataSource));
      assertTrue(tableHasForeignKey(dataSource, "tbClasse", "tbCategoria"));
      assertForeignKeyRejectsUnknownCategory(dataSource);
    }
  }

  @Test
  void shouldRejectConcurrentEquivalentActiveCategories() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      insertUser(dataSource);

      List<Boolean> results = runConcurrentInserts(dataSource, connection -> {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbCategoria (
                nmCategoria, nmCategoriaNormalizado, flAtivo, dtCadastro, cdUsuarioCadastro)
            VALUES (?, ?, b'1', NOW(6), 1)
            """)) {
          String name = Thread.currentThread().getName().endsWith("1") ? "São Paulo" : "Sao Paulo";
          statement.setString(1, name);
          statement.setString(2, "sao paulo");
          statement.executeUpdate();
        }
      }, "uk_tbCategoria_nmCategoriaAtiva");

      assertConcurrentOutcome(results, 1);
      assertEquals(1, count(dataSource, "tbCategoria", "nmCategoriaNormalizado = 'sao paulo' AND flAtivo = b'1'"));
    }
  }

  @Test
  void shouldRejectConcurrentEquivalentActiveClassesInSameCategory() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      insertUser(dataSource);
      int categoryId = insertCategory(dataSource, "Concurrent Category");

      List<Boolean> results = runConcurrentInserts(dataSource, connection -> {
        insertClass(connection, categoryId, "Classe-A", "classe-a", "COMUM");
      }, "uk_tbClasse_categoriaNomeAtiva");

      assertConcurrentOutcome(results, 1);
      assertEquals(1, count(dataSource, "tbClasse",
          "cdCategoria = " + categoryId + " AND nmClasseNormalizado = 'classe-a' AND flAtivo = b'1'"));
    }
  }

  @Test
  void shouldRejectConcurrentOverallClassesInSameCategory() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      insertUser(dataSource);
      int categoryId = insertCategory(dataSource, "Concurrent Overall Category");

      List<Boolean> results = runConcurrentInserts(dataSource, connection -> {
        String suffix = Thread.currentThread().getName().endsWith("1") ? " A" : " B";
        insertClass(connection, categoryId, "Overall" + suffix, "overall" + suffix.toLowerCase(), "OVERALL");
      }, "uk_tbClasse_categoriaOverallAtiva");

      assertConcurrentOutcome(results, 1);
      assertEquals(1, count(dataSource, "tbClasse",
          "cdCategoria = " + categoryId + " AND tipoClasse = 'OVERALL' AND flAtivo = b'1'"));
    }
  }

  @Test
  void shouldPersistSupportCatalogsAndEnforceMysqlConstraints() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      OrganizadorRepository organizadorRepository = context.getBean(OrganizadorRepository.class);
      PaisRepository paisRepository = context.getBean(PaisRepository.class);
      SubdivisaoRepository subdivisaoRepository = context.getBean(SubdivisaoRepository.class);
      insertUser(dataSource);

      Organizador inactive = new Organizador("Federação Águia", 1);
      inactive.inativar(1);
      organizadorRepository.saveAndFlush(inactive);
      Organizador active = organizadorRepository.saveAndFlush(new Organizador("Federacao Aguia", 1));
      assertEquals("federacao aguia", active.getNmOrganizadorNormalizado());
      assertThrowsDataIntegrity(
          () -> organizadorRepository.saveAndFlush(new Organizador("FEDERAÇÃO ÁGUIA", 1)));

      Pais firstCountry = paisRepository.saveAndFlush(new Pais("ZZ", "ZZZ", "Test Country One"));
      Pais secondCountry = paisRepository.saveAndFlush(new Pais("ZY", "ZYY", "Test Country Two"));
        deactivate(dataSource, "tbPais", "cdPais = " + secondCountry.getCdPais());
        assertEquals(1, count(dataSource, "tbPais", "codigoIso2 IN ('ZZ', 'ZY') AND flAtivo = b'1'"));
      assertThrowsDataIntegrity(() -> paisRepository.saveAndFlush(new Pais("ZZ", "ZZX", "Duplicate Iso2")));
      assertThrowsDataIntegrity(() -> paisRepository.saveAndFlush(new Pais("ZX", "ZZZ", "Duplicate Iso3")));

        Subdivisao firstSubdivision = subdivisaoRepository.saveAndFlush(
          new Subdivisao(firstCountry, "ZZ-ONE", "Test Subdivision One"));
      subdivisaoRepository.saveAndFlush(new Subdivisao(secondCountry, "ZZ-ONE", "Test Subdivision Two"));
        deactivate(dataSource, "tbSubdivisao", "cdSubdivisao = " + firstSubdivision.getCdSubdivisao());
        assertEquals(1, count(dataSource, "tbSubdivisao", "codigoIso = 'ZZ-ONE' AND flAtivo = b'1'"));
      assertThrowsDataIntegrity(() -> subdivisaoRepository.saveAndFlush(
          new Subdivisao(firstCountry, "ZZ-ONE", "Duplicate Same Country")));
      assertForeignKeyRejectsUnknownCountry(dataSource);

      assertEquals(249, count(dataSource, "tbPais", "codigoIso2 <> 'ZZ' AND codigoIso2 <> 'ZY'"));
      assertEquals(5046, count(dataSource, "tbSubdivisao", "codigoIso <> 'ZZ-ONE'"));
      assertEquals(1, count(dataSource, "tbPais", "codigoIso2 = 'BR' AND codigoIso3 = 'BRA'"));
      assertTrue(count(dataSource, "tbSubdivisao", "codigoIso = 'BR-SP'") > 0);
      assertTrue(count(dataSource, "tbPais", "codigoIso2 = 'US'") > 0);
    }
  }

  @Test
  void shouldRejectConcurrentEquivalentActiveOrganizers() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      insertUser(dataSource);

      List<Boolean> results = runConcurrentInserts(dataSource, connection -> {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbOrganizador (
                nmOrganizador, nmOrganizadorNormalizado, flAtivo, dtCadastro, cdUsuarioCadastro)
            VALUES (?, 'federacao aguia', b'1', NOW(6), 1)
            """)) {
          statement.setString(1, Thread.currentThread().getName().endsWith("1")
              ? "Federação Águia" : "Federacao Aguia");
          statement.executeUpdate();
        }
      }, "uk_tbOrganizador_nmOrganizadorAtiva");

      assertConcurrentOutcome(results, 1);
      assertEquals(1, count(dataSource, "tbOrganizador",
          "nmOrganizadorNormalizado = 'federacao aguia' AND flAtivo = b'1'"));
    }
  }

  @Test
  void shouldPersistCampeonatoAndEnforceMysqlCoreConstraints() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      OrganizadorRepository organizadorRepository = context.getBean(OrganizadorRepository.class);
      PaisRepository paisRepository = context.getBean(PaisRepository.class);
      SubdivisaoRepository subdivisaoRepository = context.getBean(SubdivisaoRepository.class);
      UsuarioRepository usuarioRepository = context.getBean(UsuarioRepository.class);
      CampeonatoRepository campeonatoRepository = context.getBean(CampeonatoRepository.class);

      Usuario criador = usuarioRepository.saveAndFlush(
          new Usuario("campeonato.user", "password", "Campeonato User", "campeonato.user@example.com", 1));
      Organizador organizador = organizadorRepository.saveAndFlush(new Organizador("Federação Águia", criador.getCdUsuario()));
      Pais brasil = paisRepository.saveAndFlush(new Pais("ZZ", "ZZZ", "Test Country"));
      Subdivisao saoPaulo = subdivisaoRepository.saveAndFlush(new Subdivisao(brasil, "ZZ-SP", "Test Subdivision"));

      Campeonato campeonato = campeonatoRepository.saveAndFlush(new Campeonato(
          "  Campeonato   Paulista  ",
          organizador,
          brasil,
          saoPaulo,
          "Ginásio Central",
          java.time.LocalDate.of(2026, 9, 20),
          java.time.LocalDate.of(2026, 9, 21),
          criador,
          criador.getCdUsuario()));

      assertEquals("campeonato paulista", campeonato.getNmCompeticaoNormalizado());
      assertEquals(CampeonatoStatus.ATIVO, campeonato.getStatus());
      assertEquals("varchar", columnType(dataSource, "tbCompeticao", "status"));
      assertEquals("bigint", columnType(dataSource, "tbCompeticao", "lockVersion"));

      Campeonato loaded = campeonatoRepository.findById(campeonato.getCdCompeticao()).orElseThrow();
      Long versionBefore = loaded.getLockVersion();
      assertNotNull(versionBefore);
      loaded.cancelar(criador.getCdUsuario());
      Campeonato updated = campeonatoRepository.saveAndFlush(loaded);
      assertTrue(updated.getLockVersion() > versionBefore);

      int canceledCount = count(dataSource, "tbCompeticao",
          "nmCompeticaoNormalizado = 'campeonato paulista' AND status = 'CANCELADO'");
      assertEquals(1, canceledCount);

      insertCampeonato(
          dataSource,
          organizador.getCdOrganizador(),
          brasil.getCdPais(),
          saoPaulo.getCdSubdivisao(),
          criador.getCdUsuario(),
          "campeonato paulista 2",
          "campeonato paulista 2",
          "ATIVO",
          java.sql.Date.valueOf("2026-09-22"),
          java.sql.Date.valueOf("2026-09-21"),
          "ck_tbCompeticao_periodo");

      insertCampeonato(
          dataSource,
          organizador.getCdOrganizador(),
          brasil.getCdPais(),
          saoPaulo.getCdSubdivisao(),
          criador.getCdUsuario(),
          "campeonato paulista 3",
          "campeonato paulista 3",
          "INVALIDO",
          java.sql.Date.valueOf("2026-09-20"),
          java.sql.Date.valueOf("2026-09-21"),
          "ck_tbCompeticao_status");

      int paisUsa = insertCountry(dataSource, "ZY", "ZYY", "Country ZY");
      int subdivisaoOutroPais = insertSubdivision(dataSource, paisUsa, "ZY-AA", "Other Country Subdivision");

      insertCampeonato(
          dataSource,
          organizador.getCdOrganizador(),
          brasil.getCdPais(),
          subdivisaoOutroPais,
          criador.getCdUsuario(),
          "campeonato invalido",
          "campeonato invalido",
          "ATIVO",
          java.sql.Date.valueOf("2026-09-20"),
          java.sql.Date.valueOf("2026-09-21"),
          "fk_tbCompeticao_cdPaisCdSubdivisao");
    }
  }

  @Test
  void shouldRejectConcurrentEquivalentActiveCampeonatos() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      DataSource dataSource = context.getBean(DataSource.class);
      int userId = insertUserAndReturnId(dataSource, "conc.user", "conc.user@example.com");
      int organizerId = insertOrganizer(dataSource, "Federação Águia", "federacao aguia", userId);
      int countryId = insertCountry(dataSource, "ZX", "ZXX", "Country ZX");
      int subdivisionId = insertSubdivision(dataSource, countryId, "ZX-1", "Subdivision ZX-1");

      List<Boolean> withSubdivision = runConcurrentInserts(dataSource, connection -> {
        insertCampeonato(connection,
            organizerId,
            countryId,
            subdivisionId,
            userId,
            "Campeonato Único",
            "campeonato unico",
            "ATIVO",
            java.sql.Date.valueOf("2026-10-10"),
            java.sql.Date.valueOf("2026-10-11"));
      }, "uk_tbCompeticao_identidadeAtiva");

      assertConcurrentOutcome(withSubdivision, 1);
      assertEquals(1, count(dataSource, "tbCompeticao",
          "nmCompeticaoNormalizado = 'campeonato unico' AND status = 'ATIVO'"));

      List<Boolean> withoutSubdivision = runConcurrentInserts(dataSource, connection -> {
        insertCampeonato(connection,
            organizerId,
            countryId,
            null,
            userId,
            "Campeonato Sem Subdivisão",
            "campeonato sem subdivisao",
            "ATIVO",
            java.sql.Date.valueOf("2026-10-12"),
            java.sql.Date.valueOf("2026-10-12"));
      }, "uk_tbCompeticao_identidadeAtiva");

      assertConcurrentOutcome(withoutSubdivision, 1);
      assertEquals(1, count(dataSource, "tbCompeticao",
          "nmCompeticaoNormalizado = 'campeonato sem subdivisao' AND status = 'ATIVO' AND cdSubdivisao IS NULL"));

      insertCampeonato(
          dataSource,
          organizerId,
          countryId,
          subdivisionId,
          userId,
          "Campeonato Cancelado",
          "campeonato cancelado",
          "CANCELADO",
          java.sql.Date.valueOf("2026-10-14"),
          java.sql.Date.valueOf("2026-10-14"),
          null);

      insertCampeonato(
          dataSource,
          organizerId,
          countryId,
          subdivisionId,
          userId,
          "Campeonato Cancelado 2",
          "campeonato cancelado",
          "CANCELADO",
          java.sql.Date.valueOf("2026-10-14"),
          java.sql.Date.valueOf("2026-10-14"),
          null);

      assertEquals(2, count(dataSource, "tbCompeticao",
          "nmCompeticaoNormalizado = 'campeonato cancelado' AND status = 'CANCELADO'"));
    }
  }

  private void insertUser(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbUsuario (login, senha, nome, email, dataCadastro, flAtivo, cdTpAcesso)
            VALUES ('catalogo-user', 'password', 'Catalogo User', 'catalogo@example.com', NOW(), b'1', 1)
            """)) {
      statement.executeUpdate();
    }
  }

  private int insertUserAndReturnId(DataSource dataSource, String login, String email) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbUsuario (login, senha, nome, email, dataCadastro, flAtivo, cdTpAcesso)
            VALUES (?, 'password', ?, ?, NOW(6), b'1', 1)
            """, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, login);
      statement.setString(2, "Name " + login);
      statement.setString(3, email);
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        assertTrue(keys.next());
        return keys.getInt(1);
      }
    }
  }

  private int insertOrganizer(
      DataSource dataSource, String name, String normalizedName, int userId) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbOrganizador (
                nmOrganizador, nmOrganizadorNormalizado, flAtivo, dtCadastro, cdUsuarioCadastro)
            VALUES (?, ?, b'1', NOW(6), ?)
            """, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, name);
      statement.setString(2, normalizedName);
      statement.setInt(3, userId);
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        assertTrue(keys.next());
        return keys.getInt(1);
      }
    }
  }

  private int insertCountry(DataSource dataSource, String iso2, String iso3, String name) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbPais (codigoIso2, codigoIso3, nmPais, flAtivo, dtCadastro)
            VALUES (?, ?, ?, b'1', NOW(6))
            """, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, iso2);
      statement.setString(2, iso3);
      statement.setString(3, name);
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        assertTrue(keys.next());
        return keys.getInt(1);
      }
    }
  }

  private int insertSubdivision(DataSource dataSource, int countryId, String isoCode, String name) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbSubdivisao (cdPais, codigoIso, nmSubdivisao, flAtivo, dtCadastro)
            VALUES (?, ?, ?, b'1', NOW(6))
            """, Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, countryId);
      statement.setString(2, isoCode);
      statement.setString(3, name);
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        assertTrue(keys.next());
        return keys.getInt(1);
      }
    }
  }

  private int insertCategory(DataSource dataSource, String name) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbCategoria (
                nmCategoria, nmCategoriaNormalizado, flAtivo, dtCadastro, cdUsuarioCadastro)
            VALUES (?, ?, b'1', NOW(6), 1)
            """, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, name);
      statement.setString(2, name.toLowerCase());
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        assertTrue(keys.next());
        return keys.getInt(1);
      }
    }
  }

  private void insertCampeonato(
      DataSource dataSource,
      int organizerId,
      int countryId,
      Integer subdivisionId,
      int creatorId,
      String name,
      String normalizedName,
      String status,
      java.sql.Date startDate,
      java.sql.Date endDate,
      String expectedConstraint) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      try {
        insertCampeonato(connection, organizerId, countryId, subdivisionId, creatorId, name, normalizedName, status,
            startDate, endDate);
      } catch (SQLException exception) {
        if (expectedConstraint == null) {
          throw exception;
        }
        assertTrue("23000".equals(exception.getSQLState()) || "HY000".equals(exception.getSQLState()),
            () -> "SQLState inesperado: " + exception.getSQLState());
        assertTrue(exception.getMessage().contains(expectedConstraint),
            () -> "Constraint inesperada: " + exception.getMessage());
        return;
      }
      if (expectedConstraint != null) {
        throw new AssertionError("Era esperada violação da constraint " + expectedConstraint);
      }
    }
  }

  private void insertCampeonato(
      Connection connection,
      int organizerId,
      int countryId,
      Integer subdivisionId,
      int creatorId,
      String name,
      String normalizedName,
      String status,
      java.sql.Date startDate,
      java.sql.Date endDate) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("""
        INSERT INTO tbCompeticao (
            nmCompeticao,
            nmCompeticaoNormalizado,
            cdOrganizador,
            cdPais,
            cdSubdivisao,
            local,
            dtInicio,
            dtFim,
            status,
            cdCriador,
            dtCadastro,
            cdUsuarioCadastro,
            lockVersion)
        VALUES (?, ?, ?, ?, ?, 'Arena', ?, ?, ?, ?, NOW(6), ?, 0)
        """)) {
      statement.setString(1, name);
      statement.setString(2, normalizedName);
      statement.setInt(3, organizerId);
      statement.setInt(4, countryId);
      if (subdivisionId == null) {
        statement.setNull(5, java.sql.Types.INTEGER);
      } else {
        statement.setInt(5, subdivisionId);
      }
      statement.setDate(6, startDate);
      statement.setDate(7, endDate);
      statement.setString(8, status);
      statement.setInt(9, creatorId);
      statement.setInt(10, creatorId);
      statement.executeUpdate();
    }
  }

  private void insertClass(Connection connection, int categoryId, String name, String normalized, String type)
      throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("""
        INSERT INTO tbClasse (
            cdCategoria, nmClasse, nmClasseNormalizado, tipoClasse, flAtivo, dtCadastro, cdUsuarioCadastro)
        VALUES (?, ?, ?, ?, b'1', NOW(6), 1)
        """)) {
      statement.setInt(1, categoryId);
      statement.setString(2, name);
      statement.setString(3, normalized);
      statement.setString(4, type);
      statement.executeUpdate();
    }
  }

  private List<Boolean> runConcurrentInserts(
      DataSource dataSource, ConcurrentInsert insert, String expectedConstraintName) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<Boolean>> futures = new ArrayList<>();
    try {
      for (int index = 0; index < 2; index++) {
        futures.add(executor.submit(() -> {
          ready.countDown();
          assertTrue(start.await(10, TimeUnit.SECONDS));
          try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
              insert.execute(connection);
              connection.commit();
              return true;
            } catch (SQLException exception) {
              connection.rollback();
              assertEquals("23000", exception.getSQLState());
              assertTrue(exception.getMessage().contains(expectedConstraintName),
                  () -> "Constraint inesperada: " + exception.getMessage());
              if (exception.getMessage().contains(expectedConstraintName)) {
                return false;
              }
              throw new AssertionError("A violação não corresponde à constraint esperada", exception);
            }
          }
        }));
      }
      assertTrue(ready.await(10, TimeUnit.SECONDS));
      start.countDown();
      List<Boolean> results = new ArrayList<>();
      for (Future<Boolean> future : futures) {
        results.add(future.get(30, TimeUnit.SECONDS));
      }
      return results;
    } finally {
      start.countDown();
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
    }
  }

  private void assertConcurrentOutcome(List<Boolean> results, int expectedSuccesses) {
    assertEquals(2, results.size());
    assertEquals(expectedSuccesses, results.stream().filter(Boolean.TRUE::equals).count());
    assertEquals(1, results.stream().filter(result -> !result).count());
  }

  private int count(DataSource dataSource, String tableName, String predicate) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + predicate)) {
      assertTrue(resultSet.next());
      return resultSet.getInt(1);
    }
  }

  private void deactivate(DataSource dataSource, String tableName, String predicate) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      assertEquals(1, statement.executeUpdate("UPDATE " + tableName + " SET flAtivo = b'0' WHERE " + predicate));
    }
  }

  private void assertForeignKeyRejectsUnknownCategory(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbClasse (
                cdCategoria, nmClasse, nmClasseNormalizado, tipoClasse, flAtivo, dtCadastro, cdUsuarioCadastro)
            VALUES (999999, 'Orphan', 'orphan', 'COMUM', b'1', NOW(6), 1)
            """)) {
      assertThrowsSqlIntegrity(statement::executeUpdate);
    }
  }

  private void assertForeignKeyRejectsUnknownCountry(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbSubdivisao (cdPais, codigoIso, nmSubdivisao, flAtivo, dtCadastro)
            VALUES (999999, 'ZZ-ORPHAN', 'Orphan Subdivision', b'1', NOW(6))
            """)) {
      assertThrowsSqlIntegrity(statement::executeUpdate);
    }
  }

  private void assertThrowsDataIntegrity(org.junit.jupiter.api.function.Executable executable) {
    org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class, executable);
  }

  private void assertThrowsSqlIntegrity(org.junit.jupiter.api.function.Executable executable) {
    SQLException exception = org.junit.jupiter.api.Assertions.assertThrows(SQLException.class, executable);
    assertEquals("23000", exception.getSQLState());
  }

  @FunctionalInterface
  private interface ConcurrentInsert {
    void execute(Connection connection) throws SQLException;
  }

  private String columnType(DataSource dataSource, String tableName, String columnName) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT DATA_TYPE FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
            """)) {
      statement.setString(1, tableName);
      statement.setString(2, columnName);
      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        return resultSet.getString(1);
      }
    }
  }

  private String enumValue(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT tipoClasse FROM tbClasse WHERE tipoClasse = 'OVERALL'")) {
      assertTrue(resultSet.next());
      return resultSet.getString(1);
    }
  }

  private boolean tableHasForeignKey(DataSource dataSource, String tableName, String referencedTable)
      throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*) FROM information_schema.referential_constraints
            WHERE constraint_schema = DATABASE()
              AND table_name = ?
              AND referenced_table_name = ?
            """)) {
      statement.setString(1, tableName);
      statement.setString(2, referencedTable);
      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        return resultSet.getInt(1) > 0;
      }
    }
  }

  private ConfigurableApplicationContext runApplication() {
    return new SpringApplicationBuilder(SrvTeamApplication.class)
        .initializers(context -> TestPropertyValues.of(
            "DB_URL=" + MYSQL.getJdbcUrl(),
            "DB_USERNAME=" + MYSQL.getUsername(),
            "DB_PASSWORD=" + MYSQL.getPassword(),
            "spring.datasource.url=" + MYSQL.getJdbcUrl(),
            "spring.datasource.username=" + MYSQL.getUsername(),
            "spring.datasource.password=" + MYSQL.getPassword(),
            "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.flyway.enabled=true",
            "spring.flyway.locations=classpath:db/migration",
            "server.port=0",
            "JWT_SECRET=" + JWT_SECRET,
            "jwt.expiration=86400000"
        ).applyTo(context.getEnvironment()))
        .run();
  }

  private void migrateSchema() {
    Flyway.configure()
        .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();
  }

  private void assertTableExists(DataSource dataSource, String databaseName, String tableName)
      throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = ? AND table_name = ?
            """)) {
      statement.setString(1, databaseName);
      statement.setString(2, tableName);

      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt(1));
      }
    }
  }

  private void assertFlywayHistoryPresent(DataSource dataSource, String databaseName, String tableName)
      throws SQLException {
    assertTableExists(dataSource, databaseName, tableName);
  }

  private void assertMigrationVersionPresent(DataSource dataSource, String version) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = ? AND success = TRUE
            """)) {
      statement.setString(1, version);

      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt(1));
      }
    }
  }

  private void assertDecimalPontuacaoColumnMetadata(
      DataSource dataSource,
      String databaseName,
      String tableName,
      String columnName) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT DATA_TYPE, NUMERIC_PRECISION, NUMERIC_SCALE
            FROM information_schema.columns
            WHERE table_schema = ? AND table_name = ? AND column_name = ?
            """)) {
      statement.setString(1, databaseName);
      statement.setString(2, tableName);
      statement.setString(3, columnName);

      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        assertEquals("decimal", resultSet.getString("DATA_TYPE"));
        assertEquals(10, resultSet.getInt("NUMERIC_PRECISION"));
        assertEquals(3, resultSet.getInt("NUMERIC_SCALE"));
      }
    }
  }
}
