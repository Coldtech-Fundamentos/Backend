package com.frigora.frigoraplatform.shared.infrastructure.persistence.jpa.configuration.strategy;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Snake Case With Pluralized Table Physical Naming Strategy
 *
 * @summary PhysicalNamingStrategy implementation that converts entity names to snake_case and table
 *     names to pluralized snake_case.
 * @since 1.0.0
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {
    private static final Pattern CAMEL_CASE = Pattern.compile("([a-z])([A-Z])");

    @Override
    public Identifier toPhysicalCatalogName(
            Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(identifier));
    }

    @Override
    public Identifier toPhysicalSequenceName(
            Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convert identifier to snake case
     *
     * @param identifier Identifier
     * @return Identifier
     */
    private Identifier toSnakeCase(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String newName =
                CAMEL_CASE
                        .matcher(identifier.getText())
                        .replaceAll("$1_$2")
                        .toLowerCase(Locale.ROOT);
        return Identifier.toIdentifier(newName, identifier.isQuoted());
    }

    /**
     * Convert identifier to plural
     *
     * @param identifier Identifier
     * @return Identifier
     */
    private Identifier toPlural(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String newName = pluralize(identifier.getText());
        return Identifier.toIdentifier(newName, identifier.isQuoted());
    }
}
