package com.example.srvteam.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.srvteam.SrvTeamApplication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FlywayMySqlIntegrationIT {

  private static final String BASELINE_VERSION = "20250815";
  private static final String FOLLOW_UP_VERSION = "20250816";
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
  void shouldStartApplicationOnCleanMysqlUsingFlywayMigrations() throws Exception {
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
      assertTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbUsuario");
      assertTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbCompeticao");
      assertTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbPontuacaoHist");
      assertMigrationVersionPresent(context.getBean(DataSource.class), BASELINE_VERSION);
      assertMigrationVersionPresent(context.getBean(DataSource.class), FOLLOW_UP_VERSION);
      assertDecimalPontuacaoColumnMetadata(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbPontuacao", "pontuacao");
    }
  }

  @Test
  void shouldBaselineExistingSchemaAndStartApplication() throws Exception {
    createRepresentativeLegacySchema();
    migrateSchema();

    try (ConfigurableApplicationContext context = runApplication()) {
      assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
      assertTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbUsuario");
      assertTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbTimeProfissional");
      assertFlywayHistoryPresent(context.getBean(DataSource.class), "flyway_schema_history");
      assertBaselineEntryPresent(context.getBean(DataSource.class), BASELINE_VERSION);
      assertMigrationVersionPresent(context.getBean(DataSource.class), FOLLOW_UP_VERSION);
      assertDecimalPontuacaoColumnMetadata(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbPontuacao", "pontuacao");
      assertLegacyTableExists(context.getBean(DataSource.class), MYSQL.getDatabaseName(), "tbTimeProfissional");
    }
  }

  private ConfigurableApplicationContext runApplication() {
    return new SpringApplicationBuilder(SrvTeamApplication.class)
        .properties(applicationProperties())
        .run();
  }

  private Map<String, Object> applicationProperties() {
    return Map.ofEntries(
        Map.entry("server.port", "0"),
        Map.entry("DB_URL", MYSQL.getJdbcUrl()),
        Map.entry("DB_USERNAME", MYSQL.getUsername()),
        Map.entry("DB_PASSWORD", MYSQL.getPassword()),
        Map.entry("JWT_SECRET", JWT_SECRET),
        Map.entry("spring.datasource.url", MYSQL.getJdbcUrl()),
        Map.entry("spring.datasource.username", MYSQL.getUsername()),
        Map.entry("spring.datasource.password", MYSQL.getPassword()),
        Map.entry("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"),
        Map.entry("spring.jpa.hibernate.ddl-auto", "validate"),
        Map.entry("spring.flyway.enabled", "true"),
        Map.entry("spring.flyway.baseline-on-migrate", "true"),
        Map.entry("spring.flyway.baseline-version", BASELINE_VERSION),
        Map.entry("spring.flyway.locations", "classpath:db/migration"),
        Map.entry("jwt.expiration", "86400000"));
  }

  private void createRepresentativeLegacySchema() throws SQLException {
    try (Connection connection = DriverManager.getConnection(
        MYSQL.getJdbcUrl(),
        MYSQL.getUsername(),
        MYSQL.getPassword())) {
      ScriptUtils.executeSqlScript(connection,
          new ClassPathResource("db/legacy/V20250814__legacy_schema_before_flyway.sql"));
    }
  }

  private void migrateSchema() {
    Flyway.configure()
        .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
        .baselineOnMigrate(true)
        .baselineVersion(MigrationVersion.fromVersion(BASELINE_VERSION))
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

  private void assertLegacyTableExists(DataSource dataSource, String databaseName, String tableName)
      throws SQLException {
    assertTableExists(dataSource, databaseName, tableName);
  }

  private void assertFlywayHistoryPresent(DataSource dataSource, String tableName) throws SQLException {
    assertTableExists(dataSource, MYSQL.getDatabaseName(), tableName);
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

  private void assertBaselineEntryPresent(DataSource dataSource, String version) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("""
            SELECT type, version, description
            FROM flyway_schema_history
            WHERE version = ?
            ORDER BY installed_rank
            """)) {
      statement.setString(1, version);

      try (ResultSet resultSet = statement.executeQuery()) {
        List<String> types = new ArrayList<>();
        while (resultSet.next()) {
          types.add(resultSet.getString("type"));
          assertNotNull(resultSet.getString("version"));
          assertNotNull(resultSet.getString("description"));
        }
        assertTrue(types.contains("BASELINE"));
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
