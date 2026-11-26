package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import java.util.UUID;

class IamMigrationTests {
    private JdbcDataSource database() {
        var database = new JdbcDataSource();
        database.setURL("jdbc:h2:mem:iam_" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        return database;
    }

    @Test
    void migrationRetainsExplicitImportedIdsAndDoesNotReapply() throws Exception {
        var database = database();
        var flyway = Flyway.configure().dataSource(database).locations("classpath:db/migration")
                .cleanDisabled(true).baselineOnMigrate(false).load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        try (var connection = database.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO users (id, username, password, role, created_at, updated_at)
                    VALUES (1200, 'synthetic-import', 'synthetic-hash', 'Owner',
                            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """);
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            try (var rows = statement.executeQuery(
                    "SELECT id FROM users WHERE username = 'synthetic-import'")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(1200);
            }
        }
    }

    @Test
    void refusesExistingUnversionedSchema() throws Exception {
        var database = database();
        try (var connection = database.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id INT PRIMARY KEY)");
        }
        var flyway = Flyway.configure().dataSource(database).locations("classpath:db/migration")
                .baselineOnMigrate(false).load();
        assertThatThrownBy(flyway::migrate).isInstanceOf(FlywayException.class);
    }
}
