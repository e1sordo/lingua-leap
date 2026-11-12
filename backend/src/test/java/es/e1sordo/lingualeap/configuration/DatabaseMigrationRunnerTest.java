package es.e1sordo.lingualeap.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMigrationRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    void freshDatabaseReceivesFullMigration() throws Exception {
        final Path dbFile = tempDir.resolve("fresh.db");

        DatabaseMigrationRunner.migrate("jdbc:sqlite:" + dbFile);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            assertEquals(4, countRows(connection, "languages"));
            assertEquals(1, countRows(connection, "courses"));
            assertEquals(2, countRows(connection, "course_translation_languages"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '1'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '2'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '3'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '4'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '5'"));
            assertTableExists(connection, "meaning_translations");
            assertTableExists(connection, "collocation_translations");
            assertColumnExists(connection, "words", "course_id");
            assertColumnExists(connection, "vocabulary_lists", "smart_type");
            assertColumnExists(connection, "words_to_add_later", "course_id");

            // courses.created_at must be epoch millis (the format the SQLite JDBC stack can read back)
            assertEquals("integer", scalarString(connection, "SELECT typeof(created_at) FROM courses"));
            assertEquals(LocalDate.now(), readCreatedAtAsLocalDate(connection));

            // rollback journal mode must be enforced (WAL breaks Hibernate's isolated ID generation)
            assertEquals("delete", scalarString(connection, "PRAGMA journal_mode"));

            // V5: id columns are INTEGER PRIMARY KEY AUTOINCREMENT (rowid aliases for IDENTITY generation)
            assertTableSqlContains(connection, "courses", "AUTOINCREMENT");
            assertTableSqlContains(connection, "words", "AUTOINCREMENT");
            assertTableSqlContains(connection, "meanings", "AUTOINCREMENT");
            // V5: table-based ID generator tables are dropped
            assertEquals(0, countRows(connection,
                    "sqlite_master WHERE type = 'table' AND name LIKE '%\\_seq' ESCAPE '\\'"));
            // data survived the rebuild with ids preserved
            assertEquals(2, scalarInt(connection, "SELECT COUNT(*) FROM course_translation_languages WHERE id IN (1, 2)"));
        }
    }

    @Test
    void legacyDatabaseIsMigratedWithoutDataLoss() throws Exception {
        final Path dbFile = tempDir.resolve("legacy.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            // given: legacy schema (as V1 defines it) filled with sample data, V1 marked as applied
            DatabaseMigrationRunner.applyScript(connection, "V1__legacy_baseline.sql");
            fillLegacyData(connection);
            try (Statement s = connection.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS schema_migrations " +
                          "(version varchar(255) not null, applied_at timestamp not null, primary key (version))");
            }
            markApplied(connection, "1");
        }

        // when
        DatabaseMigrationRunner.migrate("jdbc:sqlite:" + dbFile);

        // then
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '2'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '4'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM schema_migrations WHERE version = '5'"));

            // V5: legacy data preserved through the identity-column rebuild
            assertTableSqlContains(connection, "words", "AUTOINCREMENT");
            assertEquals(0, countRows(connection,
                    "sqlite_master WHERE type = 'table' AND name LIKE '%\\_seq' ESCAPE '\\'"));

            assertEquals(3, countRows(connection, "words"));
            assertEquals(3, countRows(connection, "meanings"));
            assertEquals(2, countRows(connection, "meaning_collocations"));
            assertEquals(2, countRows(connection, "meaning_contexts"));
            assertEquals(3, countRows(connection, "sm2_meta"));

            assertEquals(3, countRows(connection, "meaning_translations WHERE language_code = 'ru'"));
            assertEquals(2, countRows(connection, "meaning_translations WHERE language_code = 'en'"));
            assertEquals(1, countRows(connection, "collocation_translations WHERE language_code = 'ru'"));
            assertEquals(1, countRows(connection, "collocation_translations WHERE language_code = 'en'"));

            assertEquals(3, countRows(connection, "words WHERE course_id = 1"));
            assertEquals(3, countRows(connection, "vocabulary_lists WHERE course_id = 1"));
            assertEquals(1, countRows(connection, "vocabulary_lists WHERE smart_type = 'RECENTLY_ADDED'"));
            assertEquals(1, countRows(connection, "vocabulary_lists WHERE smart_type = 'PROBLEM_WORDS'"));
            assertEquals(2, countRows(connection, "words_to_add_later WHERE course_id = 1"));

            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM meaning_translations WHERE meaning_id = 101 AND language_code = 'ru' AND text = 'скучный'"));
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM meaning_translations WHERE meaning_id = 101 AND language_code = 'en' AND text = 'boring'"));

            // IDs of legacy rows are preserved
            assertEquals(3, scalarInt(connection, "SELECT COUNT(*) FROM words WHERE id IN (11, 12, 13)"));
            assertEquals(3, scalarInt(connection, "SELECT COUNT(*) FROM meanings WHERE id IN (101, 102, 103)"));

            // dates are preserved
            assertEquals("2023-05-01", scalarString(connection, "SELECT added FROM words WHERE id = 11"));
            assertEquals("2024-12-31", scalarString(connection, "SELECT planned_review FROM sm2_meta WHERE id = 501"));

            // unique index on (course_id, word) is in place
            assertTrue(scalarInt(connection,
                    "SELECT COUNT(*) FROM pragma_index_list('words') WHERE name = 'uk_words_course_word'") > 0);

            // foreign key check returns nothing
            assertEquals(0, countRows(connection, "pragma_foreign_key_check"));

            // courses.created_at was normalized to epoch millis by V3
            assertEquals("integer", scalarString(connection, "SELECT typeof(created_at) FROM courses"));
        }
    }

    private LocalDate readCreatedAtAsLocalDate(final Connection connection) throws SQLException {
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT created_at FROM courses WHERE id = 1")) {
            assertTrue(rs.next());
            return rs.getTimestamp(1).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    private void fillLegacyData(final Connection connection) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("INSERT INTO words (id, added, word) VALUES " +
                      "(11, '2023-05-01', 'aburrido'), (12, '2023-05-02', 'hola'), (13, '2023-05-02', 'mesa')");
            s.execute("INSERT INTO words_seq (next_val) VALUES (101)");
            s.execute("INSERT INTO meanings (id, word_id, pos, gender, russian_variant, english_variant, learning_status) VALUES " +
                      "(101, 11, 'ADJECTIVE', NULL, 'скучный', 'boring', 0), " +
                      "(102, 12, 'NOUN', 'FEMININE', 'привет', NULL, 1), " +
                      "(103, 13, 'NOUN', 'FEMININE', 'стол', 'table', 0)");
            s.execute("INSERT INTO meanings_seq (next_val) VALUES (201)");
            s.execute("INSERT INTO meaning_collocations (id, word_meaning_id, pattern, translation_russian, translation_english) VALUES " +
                      "(201, 101, 'estar {}', 'быть скучным', 'to be boring'), (202, 101, 'muy {}', NULL, NULL)");
            s.execute("INSERT INTO meaning_collocations_seq (next_val) VALUES (301)");
            s.execute("INSERT INTO meaning_contexts (id, word_meaning_id, sentence, translation) VALUES " +
                      "(301, 101, 'Estoy aburrido', 'Мне скучно'), (302, 101, 'Qué aburrido', 'Как скучно')");
            s.execute("INSERT INTO meaning_contexts_seq (next_val) VALUES (401)");
            s.execute("INSERT INTO vocabulary_lists (id, name, smart, added) VALUES " +
                      "(401, 'New words', 1, '2023-05-01'), (402, 'Problem words', 1, '2023-05-01'), (403, 'Favoritos', 0, '2023-05-01')");
            s.execute("INSERT INTO vocabulary_lists_seq (next_val) VALUES (501)");
            s.execute("INSERT INTO meaning_to_lists (meaning_id, list_id) VALUES (101, 401), (101, 403)");
            s.execute("INSERT INTO words_to_add_later (word, times_added) VALUES ('tarde', 2), ('calle', 1)");
            s.execute("INSERT INTO sm2_meta (id, word_id, previous_user_grade, repetition_number, total_repetitions, easiness_factor, previous_review, planned_review) VALUES " +
                      "(501, 101, 0, 0, 0, 2.5, '2023-05-01', '2024-12-31'), " +
                      "(502, 102, 3, 1, 1, 2.5, '2023-05-02', '2023-05-03'), " +
                      "(503, 103, 5, 2, 2, 2.6, '2023-05-02', '2023-05-08')");
            s.execute("INSERT INTO sm2_meta_seq (next_val) VALUES (601)");
        }
    }

    private void markApplied(final Connection connection, final String version) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("INSERT INTO schema_migrations (version, applied_at) VALUES ('" + version + "', CURRENT_TIMESTAMP)");
        }
    }

    private static long countRows(final Connection connection, final String fromExpression) throws SQLException {
        return scalarInt(connection, "SELECT COUNT(*) FROM " + fromExpression);
    }

    private static int scalarInt(final Connection connection, final String sql) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static String scalarString(final Connection connection, final String sql) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private static void assertTableExists(final Connection connection, final String table) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + table + "'")) {
            assertEquals(1, rs.getInt(1), "table " + table + " must exist");
        }
    }

    private static void assertTableSqlContains(final Connection connection, final String table, final String fragment) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + table + "' AND sql LIKE '%" + fragment + "%'")) {
            assertEquals(1, rs.getInt(1), "table " + table + " definition must contain " + fragment);
        }
    }

    private static void assertColumnExists(final Connection connection, final String table, final String column) throws SQLException {
        final List<String> columns = new ArrayList<>();
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        assertTrue(columns.contains(column), "column " + table + "." + column + " must exist");
    }
}
