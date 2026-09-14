package com.example.srvteam.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.srvteam.SrvTeamApplication;
import com.example.srvteam.catalogo.model.Categoria;
import com.example.srvteam.catalogo.model.Classe;
import com.example.srvteam.catalogo.model.TipoClasse;
import com.example.srvteam.catalogo.repository.CategoriaRepository;
import com.example.srvteam.catalogo.repository.ClasseRepository;
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
        assertEquals(1, categoriaRepository.findAll().stream().filter(Categoria::getFlAtivo).count());

      assertThrowsDataIntegrity(() -> categoriaRepository.saveAndFlush(new Categoria("Sao Paulo", 1)));

        Classe classeInativa = new Classe(categoria, "Classe-A", TipoClasse.COMUM, 1);
        classeInativa.setFlAtivo(false);
        classeRepository.saveAndFlush(classeInativa);

        Classe classe = classeRepository.saveAndFlush(new Classe(categoria, "Classe-A", TipoClasse.COMUM, 1));
      assertEquals("classe-a", classe.getNmClasseNormalizado());
      assertEquals(TipoClasse.COMUM, classe.getTipoClasse());
        assertEquals(2, classeRepository.findAll().size());
        assertEquals(1, classeRepository.findAll().stream().filter(Classe::getFlAtivo).count());

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

  private void insertUser(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO tbUsuario (login, senha, nome, email, dataCadastro, flAtivo, cdTpAcesso)
            VALUES ('catalogo-user', 'password', 'Catalogo User', 'catalogo@example.com', NOW(), b'1', 1)
            """)) {
      statement.executeUpdate();
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
    assertEquals(expectedSuccesses, results.stream().filter(Boolean::booleanValue).count());
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
