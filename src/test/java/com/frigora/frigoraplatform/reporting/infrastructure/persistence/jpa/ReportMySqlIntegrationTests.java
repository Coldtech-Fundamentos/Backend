package com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa;

import com.frigora.frigoraplatform.reporting.application.internal.commandservices.ReportCommandServiceImpl;
import com.frigora.frigoraplatform.reporting.application.internal.queryservices.ReportQueryServiceImpl;
import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.DeleteReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.UpdateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByEquipmentIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetReportByIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;
import com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories.JpaReportRepository;
import com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories.ReportRepositoryImpl;
import com.frigora.frigoraplatform.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Uses only a disposable container. Never loads application.properties or the monolith context. */
@Testcontainers(disabledWithoutDocker = true)
@SpringJUnitConfig(ReportMySqlIntegrationTests.JpaConfiguration.class)
@Transactional
class ReportMySqlIntegrationTests {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("reporting_integration_test")
            .withUsername("reporting_test").withPassword("disposable-test-password");

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = JpaReportRepository.class)
    @Import({ReportRepositoryImpl.class, ReportCommandServiceImpl.class, ReportQueryServiceImpl.class})
    static class JpaConfiguration {
        @Bean
        DataSource dataSource() {
            // Credentials and URL always come from this container, with no environment fallback.
            return new DriverManagerDataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(Report.class.getPackageName());
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.jdbc.time_zone", "UTC",
                    "hibernate.physical_naming_strategy", SnakeCaseWithPluralizedTablePhysicalNamingStrategy.class.getName()));
            return factory;
        }

        @Bean
        JpaTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    @Autowired JpaReportRepository jpa;
    @Autowired ReportRepositoryImpl repository;
    @Autowired ReportCommandServiceImpl commands;
    @Autowired ReportQueryServiceImpl queries;
    @PersistenceContext EntityManager entityManager;

    private Report create(int owner, int equipment) {
        return commands.handle(new CreateReportCommand(owner, EReportType.INSPECTION, equipment,
                "Inspection", "Summary", "Content", "https://example.test/report")).orElseThrow();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void savesAndReloadsThroughRealJpaWithExpectedPhysicalNamesAndEnumValues() {
        var report = create(101, 50);
        int id = report.getId();
        assertThat(id).isPositive();
        flushAndClear();
        var loaded = repository.findByIdAndTenantId(id, 101).orElseThrow();
        assertThat(loaded.getTenantId()).isEqualTo(101);
        assertThat(loaded.getEquipmentId()).isEqualTo(50);
        assertThat(loaded.getType()).isEqualTo(EReportType.INSPECTION);
        assertThat(loaded.getStatus()).isEqualTo(EReportStatus.IN_PROGRESS);
        Object[] row = (Object[]) entityManager.createNativeQuery(
                "select tenant_id, equipment_id, type, status, title, summary, content, url from reports where id = :id")
                .setParameter("id", id).getSingleResult();
        assertThat(((Number) row[0]).intValue()).isEqualTo(101);
        assertThat(((Number) row[1]).intValue()).isEqualTo(50);
        assertThat(row[2]).isEqualTo("INSPECTION");
        assertThat(row[3]).isEqualTo("IN_PROGRESS");
        assertThat(row[4]).isEqualTo("Inspection");
        assertThat(row[5]).isEqualTo("Summary");
        assertThat(row[6]).isEqualTo("Content");
        assertThat(row[7]).isEqualTo("https://example.test/report");
    }

    @Test
    void idLookupAndServicesExcludeTheOtherOwner() {
        var first = create(101, 50);
        var second = create(202, 50);
        int firstId = first.getId();
        int secondId = second.getId();
        flushAndClear();
        assertThat(repository.findByIdAndTenantId(firstId, 101)).isPresent();
        assertThat(repository.findByIdAndTenantId(firstId, 202)).isEmpty();
        assertThat(repository.findByIdAndTenantId(secondId, 101)).isEmpty();
        assertThat(queries.handle(new GetReportByIdQuery(secondId, 202))).isPresent();
        assertThat(queries.handle(new GetReportByIdQuery(firstId, 202))).isEmpty();
    }

    @Test
    void filtersByEquipmentAndOwnerTogether() {
        var first = create(101, 50);
        create(202, 50);
        create(101, 60);
        int id = first.getId();
        flushAndClear();
        assertThat(repository.findAllByEquipmentIdAndTenantId(50, 101))
                .extracting(Report::getId).containsExactly(id);
        assertThat(queries.handle(new GetAllReportsByEquipmentIdQuery(50, 202)))
                .extracting(Report::getTenantId).containsExactly(202);
        assertThat(repository.findAllByEquipmentIdAndTenantId(50, 303)).isEmpty();
        assertThat(repository.findAllByTenantId(101)).hasSize(2);
    }

    @Test
    void authorizedUpdatePersistsChangesAndPreservesOwner() {
        int id = create(101, 50).getId();
        flushAndClear();
        assertThat(commands.handle(new UpdateReportCommand(id, 101, 60, "Updated",
                EReportStatus.COMPLETED, "Summary", "Changed", "url"))).isPresent();
        flushAndClear();
        var loaded = jpa.findById(id).orElseThrow();
        assertThat(loaded.getTenantId()).isEqualTo(101);
        assertThat(loaded.getEquipmentId()).isEqualTo(60);
        assertThat(loaded.getTitle()).isEqualTo("Updated");
        assertThat(loaded.getStatus()).isEqualTo(EReportStatus.COMPLETED);
    }

    @Test
    void foreignUpdateAndDeleteLeaveTheStoredReportUntouched() {
        int id = create(101, 50).getId();
        flushAndClear();
        assertThat(commands.handle(new UpdateReportCommand(id, 202, 60, "Unauthorized",
                EReportStatus.CANCELED, "", "", ""))).isEmpty();
        assertThat(commands.handle(new DeleteReportCommand(id, 202))).isFalse();
        flushAndClear();
        var loaded = jpa.findById(id).orElseThrow();
        assertThat(loaded.getTenantId()).isEqualTo(101);
        assertThat(loaded.getTitle()).isEqualTo("Inspection");
        assertThat(loaded.getStatus()).isEqualTo(EReportStatus.IN_PROGRESS);
    }

    @Test
    void authorizedDeleteRemovesOnlyTheOwnedReport() {
        int firstId = create(101, 50).getId();
        int secondId = create(202, 50).getId();
        flushAndClear();
        assertThat(commands.handle(new DeleteReportCommand(firstId, 101))).isTrue();
        flushAndClear();
        assertThat(jpa.findById(firstId)).isEmpty();
        assertThat(jpa.findById(secondId)).isPresent();
        assertThat(commands.handle(new DeleteReportCommand(firstId, 101))).isFalse();
    }

    @Test
    void embeddedAuditColumnsRoundTripAndCreatedAtIsNotUpdated() {
        var report = new Report(new CreateReportCommand(101, EReportType.INSPECTION, 50,
                "Inspection", "Summary", "Content", "https://example.test/report"));
        var created = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        var updated = created.plusDays(1);
        report.getAudit().setCreatedDate(created);
        report.getAudit().setUpdatedDate(updated);
        repository.save(report);
        int id = report.getId();
        flushAndClear();
        var loaded = jpa.findById(id).orElseThrow();
        assertThat(loaded.getAudit().getCreatedDate().toInstant()).isEqualTo(created.toInstant());
        assertThat(loaded.getAudit().getUpdatedDate().toInstant()).isEqualTo(updated.toInstant());
        loaded.getAudit().setCreatedDate(created.plusDays(10));
        loaded.getAudit().setUpdatedDate(updated.plusDays(1));
        flushAndClear();
        var reloaded = jpa.findById(id).orElseThrow();
        assertThat(reloaded.getAudit().getCreatedDate().toInstant()).isEqualTo(created.toInstant());
        assertThat(reloaded.getAudit().getUpdatedDate().toInstant()).isEqualTo(updated.plusDays(1).toInstant());
    }

    @Test
    void documentsCurrentMissingAutomaticAuditListener() {
        int id = create(101, 50).getId();
        flushAndClear();
        Object[] audit = (Object[]) entityManager.createNativeQuery(
                "select created_at, updated_at from reports where id = :id").setParameter("id", id).getSingleResult();
        // Characterization of the current limitation, not a guarantee of working automatic auditing.
        assertThat(audit).containsOnlyNulls();
    }
}
