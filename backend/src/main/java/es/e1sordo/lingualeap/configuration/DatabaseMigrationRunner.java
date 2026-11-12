package es.e1sordo.lingualeap.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Minimal Flyway-like migration runner for SQLite.
 *
 * Flyway has no official SQLite support, so this runner applies versioned SQL scripts from
 * {@code db/migration} before Spring Boot (and therefore Hibernate) starts. Applied versions
 * are tracked in the {@code schema_migrations} table.
 */
@Slf4j
public final class DatabaseMigrationRunner {

    private static final String MIGRATIONS_DIR = "db/migration/";
    private static final String[] MIGRATION_FILES = {
            "V1__legacy_baseline.sql",
            "V2__multilingual_courses.sql",
            "V3__course_created_at_epoch.sql",
            "V4__resync_id_generators.sql",
            "V5__identity_columns.sql"
    };

    private DatabaseMigrationRunner() {
    }

    public static void runFromEnvironment() {
        final String dbPath = System.getenv("DB_PATH");
        if (dbPath == null || dbPath.isBlank()) {
            log.info("DB_PATH is not set -- skipping database migrations");
            return;
        }
        migrate("jdbc:sqlite:" + dbPath);
    }

    public static void migrate(final String jdbcUrl) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(true);
            createMigrationsTable(connection);
            final var appliedVersions = loadAppliedVersions(connection);

            for (final String migrationFile : MIGRATION_FILES) {
                final String version = migrationFile.substring(1, migrationFile.indexOf("__"));
                if (appliedVersions.contains(version)) {
                    log.info("Migration {} is already applied, skipping", migrationFile);
                    continue;
                }
                if ("1".equals(version) && tableExists(connection, "words")) {
                    // Existing non-empty legacy database: baseline V1 as if it had been applied.
                    log.info("Legacy schema detected -- baselining migration {}", migrationFile);
                    recordVersion(connection, version);
                    continue;
                }
                log.info("Applying migration {}", migrationFile);
                applyMigration(connection, migrationFile);
                recordVersion(connection, version);
                log.info("Migration {} applied successfully", migrationFile);
            }

            tuneSqliteForConcurrency(connection);
        } catch (final SQLException | IOException e) {
            throw new IllegalStateException("Database migration failed: " + e.getMessage(), e);
        }
    }

    /**
     * SQLite ships with busy_timeout=0 (fail immediately on any lock conflict), which surfaces
     * as SQLITE_BUSY under the default pooled connection model. A non-zero busy timeout makes
     * writers wait briefly instead. WAL mode is deliberately NOT enabled and is actively
     * reverted to DELETE: journal_mode is a persistent property of the database file, and
     * under WAL Hibernate's table ID generators break -- they perform isolated writes on a
     * second pooled connection, and the read-then-write transaction then conflicts with its
     * own generator's commit as an unrecoverable SQLITE_BUSY_SNAPSHOT.
     */
    private static void tuneSqliteForConcurrency(final Connection connection) throws SQLException {
        final String journalMode;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA journal_mode = DELETE")) {
            journalMode = resultSet.next() ? resultSet.getString(1) : "unknown";
        }
        execute(connection, "PRAGMA busy_timeout = 5000");
        log.info("SQLite journal_mode set to {}, busy_timeout set to 5000 ms", journalMode);
    }

    private static void createMigrationsTable(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS schema_migrations " +
                              "(version varchar(255) not null, applied_at timestamp not null, primary key (version))");
        }
    }

    private static java.util.Set<String> loadAppliedVersions(final Connection connection) throws SQLException {
        final var versions = new TreeSet<String>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT version FROM schema_migrations")) {
            while (resultSet.next()) {
                versions.add(resultSet.getString(1));
            }
        }
        return versions;
    }

    private static boolean tableExists(final Connection connection, final String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + tableName + "'")) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    private static void recordVersion(final Connection connection, final String version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO schema_migrations (version, applied_at) VALUES ('" + version + "', CURRENT_TIMESTAMP)");
        }
    }

    private static void applyMigration(final Connection connection, final String migrationFile) throws SQLException, IOException {
        final List<String> statements = readStatements(migrationFile);

        // PRAGMA statements cannot run inside a transaction -- execute them upfront.
        final List<String> transactional = new ArrayList<>();
        for (final String sql : statements) {
            if (sql.regionMatches(true, 0, "PRAGMA", 0, 6)) {
                execute(connection, sql);
            } else {
                transactional.add(sql);
            }
        }

        connection.setAutoCommit(false);
        try {
            for (final String sql : transactional) {
                execute(connection, sql);
            }
            connection.commit();
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    static void applyScript(final Connection connection, final String migrationFile) throws SQLException, IOException {
        for (final String sql : readStatements(migrationFile)) {
            execute(connection, sql);
        }
    }

    private static void execute(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static List<String> readStatements(final String migrationFile) throws IOException {
        final List<String> statements = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();

        try (InputStream input = DatabaseMigrationRunner.class.getClassLoader().getResourceAsStream(MIGRATIONS_DIR + migrationFile)) {
            if (input == null) {
                throw new IOException("Migration file not found on classpath: " + MIGRATIONS_DIR + migrationFile);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    buffer.append(trimmed).append(' ');
                    if (trimmed.endsWith(";")) {
                        final String statement = buffer.toString().trim();
                        statements.add(statement.substring(0, statement.length() - 1));
                        buffer.setLength(0);
                    }
                }
            }
        }

        if (!buffer.isEmpty()) {
            throw new IOException("Unterminated statement at the end of " + migrationFile);
        }
        return statements;
    }
}
