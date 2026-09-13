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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
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

  @Test
  void shouldStartApplicationOnCleanMysqlUsingFlywayMigrations() throws Exception {
    String databaseName = newDatabaseName("clean");
    createDatabase(databaseName);

    try (ConfigurableApplicationContext context = runApplication(databaseName)) {
      assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
      assertTableExists(context.getBean(DataSource.class), databaseName, "tbUsuario");
      assertTableExists(context.getBean(DataSource.class), databaseName, "tbCompeticao");
      assertTableExists(context.getBean(DataSource.class), databaseName, "tbPontuacaoHist");
      assertMigrationVersionPresent(context.getBean(DataSource.class), BASELINE_VERSION);
      assertMigrationVersionPresent(context.getBean(DataSource.class), FOLLOW_UP_VERSION);
    }
  }

  @Test
  void shouldBaselineExistingSchemaAndStartApplication() throws Exception {
    String databaseName = newDatabaseName("baseline");
    createDatabase(databaseName);
    createRepresentativeLegacySchema(databaseName);

    try (ConfigurableApplicationContext context = runApplication(databaseName)) {
      assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
      assertBaselineEntryPresent(context.getBean(DataSource.class), BASELINE_VERSION);
      assertMigrationVersionPresent(context.getBean(DataSource.class), FOLLOW_UP_VERSION);
      assertTableExists(context.getBean(DataSource.class), databaseName, "tbTimeProfissional");
    }
  }

  private ConfigurableApplicationContext runApplication(String databaseName) {
    return new SpringApplicationBuilder(SrvTeamApplication.class)
        .properties(applicationProperties(databaseName))
        .run();
  }

  private Map<String, Object> applicationProperties(String databaseName) {
    return Map.of(
        "server.port", "0",
        "spring.autoconfigure.exclude", "",
        "spring.datasource.url", jdbcUrl(databaseName),
        "spring.datasource.username", MYSQL.getUsername(),
        "spring.datasource.password", MYSQL.getPassword(),
        "spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto", "validate",
        "spring.flyway.enabled", "true",
        "spring.flyway.baseline-on-migrate", "true",
        "spring.flyway.baseline-version", BASELINE_VERSION,
        "jwt.secret", JWT_SECRET,
        "jwt.expiration", "86400000");
  }

  private void createDatabase(String databaseName) throws SQLException {
    try (Connection connection = DriverManager.getConnection(
        MYSQL.getJdbcUrl(),
        MYSQL.getUsername(),
        MYSQL.getPassword());
        Statement statement = connection.createStatement()) {
      statement.execute("CREATE DATABASE `" + databaseName + "`");
    }
  }

  private void createRepresentativeLegacySchema(String databaseName) throws SQLException {
    try (Connection connection = DriverManager.getConnection(
        jdbcUrl(databaseName),
        MYSQL.getUsername(),
        MYSQL.getPassword())) {
      ScriptUtils.executeSqlScript(connection,
          new ClassPathResource("db/migration/V20250815__baseline_existing_schema.sql"));
    }
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
            SELECT type
            FROM flyway_schema_history
            WHERE version = ?
            ORDER BY installed_rank
            """)) {
      statement.setString(1, version);

      try (ResultSet resultSet = statement.executeQuery()) {
        List<String> types = new ArrayList<>();
        while (resultSet.next()) {
          types.add(resultSet.getString(1));
        }
        assertTrue(types.contains("BASELINE"));
      }
    }
  }

  private String jdbcUrl(String databaseName) {
    return "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        .formatted(MYSQL.getHost(), MYSQL.getMappedPort(MySQLContainer.MYSQL_PORT), databaseName);
  }

  private String newDatabaseName(String prefix) {
    return "gate2_" + prefix + "_" + UUID.randomUUID().toString().replace("-", "");
  }
}
