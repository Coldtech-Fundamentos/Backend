package com.frigora.frigoraplatform.shared.infrastructure.persistence.jpa.configuration.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class PhysicalNamingStrategyTests {
    private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy =
            new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

    @Test
    void preservesCatalogAndQuotedIdentifiers() {
        var catalog = Identifier.toIdentifier("reporting_catalog", true);
        assertThat(strategy.toPhysicalCatalogName(catalog, null)).isSameAs(catalog);
        var column =
                strategy.toPhysicalColumnName(Identifier.toIdentifier("equipmentId", true), null);
        assertThat(column.getText()).isEqualTo("equipment_id");
        assertThat(column.isQuoted()).isTrue();
        var table =
                strategy.toPhysicalTableName(
                        Identifier.toIdentifier("InspectionReport", true), null);
        assertThat(table.getText()).isEqualTo("inspection_reports");
        assertThat(table.isQuoted()).isTrue();
    }

    @Test
    void acceptsAbsentIdentifiers() {
        assertThat(strategy.toPhysicalCatalogName(null, null)).isNull();
        assertThat(strategy.toPhysicalSchemaName(null, null)).isNull();
        assertThat(strategy.toPhysicalTableName(null, null)).isNull();
        assertThat(strategy.toPhysicalSequenceName(null, null)).isNull();
        assertThat(strategy.toPhysicalColumnName(null, null)).isNull();
    }

    @Test
    void casingDoesNotDependOnHostLocale() {
        var original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(
                            strategy.toPhysicalColumnName(Identifier.toIdentifier("TITLE"), null)
                                    .getText())
                    .isEqualTo("title");
        } finally {
            Locale.setDefault(original);
        }
    }
}
