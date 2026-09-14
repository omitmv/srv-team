package com.example.srvteam.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.srvteam.SrvTeamApplication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FlywayMySqlIntegrationIT {

  private static final String INITIAL_MIGRATION_VERSION = "20250815";
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
      assertDecimalPontuacaoColumnMetadata(dataSource, MYSQL.getDatabaseName(), "tbPontuacao", "pontuacao");
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
