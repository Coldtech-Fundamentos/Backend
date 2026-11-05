package com.frigora.frigoraplatform.reporting.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/** Migration smoke checks on H2 MySQL mode only. Does NOT validate a real MySQL server. */
class ReportMigrationTests {
    private JdbcDataSource isolatedDatabase() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:migration_" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void appliesVersionOneAndTracksItWithoutReapplyingOrChangingRows() throws Exception {
        var dataSource = isolatedDatabase();
        var flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                .cleanDisabled(true).baselineOnMigrate(false).load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("insert into reports (tenant_id, equipment_id, type, status, content) "
                    + "values (101, 50, 'INSPECTION', 'IN_PROGRESS', 'test content')");
            try (var result = statement.executeQuery("select * from reports")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt("id")).isPositive();
                assertThat(result.getInt("tenant_id")).isEqualTo(101);
                assertThat(result.getInt("equipment_id")).isEqualTo(50);
                assertThat(result.getString("type")).isEqualTo("INSPECTION");
                assertThat(result.getString("status")).isEqualTo("IN_PROGRESS");
                assertThat(result.getMetaData().getColumnCount()).isEqualTo(11);
                assertThat(result.getTimestamp("created_at")).isNull();
                assertThat(result.getTimestamp("updated_at")).isNull();
            }
            flyway.validate();
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            try (var result = statement.executeQuery("select count(*) from reports where tenant_id = 101")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void refusesToBaselineAnExistingUnversionedSchema() throws Exception {
        var dataSource = isolatedDatabase();
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("create table existing_data (id int primary key)");
            statement.execute("insert into existing_data values (1)");
            var flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .baselineOnMigrate(false).cleanDisabled(true).load();
            assertThatThrownBy(flyway::migrate).isInstanceOf(FlywayException.class);
            try (var result = statement.executeQuery("select count(*) from existing_data")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isEqualTo(1);
            }
        }
    }
}
