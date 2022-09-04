package com.pcalouche.springbootliquibase.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class MyLiquibaseConfiguration {
    private final LiquibaseProperties properties;

    public MyLiquibaseConfiguration(LiquibaseProperties properties) {
        this.properties = properties;
    }


    @Bean
    public SpringLiquibase liquibase(ObjectProvider<DataSource> dataSource, @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource) {
        SpringLiquibase liquibase = createSpringLiquibase(liquibaseDataSource.getIfAvailable(), dataSource.getIfUnique());
        liquibase.setChangeLog(this.properties.getChangeLog());
        liquibase.setClearCheckSums(this.properties.isClearChecksums());
        liquibase.setContexts(this.properties.getContexts());
        liquibase.setDefaultSchema(this.properties.getDefaultSchema());
        liquibase.setLiquibaseSchema(this.properties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(this.properties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(this.properties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(this.properties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(this.properties.isDropFirst());
        liquibase.setShouldRun(this.properties.isEnabled());
        liquibase.setLabels(this.properties.getLabels());
        liquibase.setChangeLogParameters(this.properties.getParameters());
        liquibase.setRollbackFile(this.properties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(this.properties.isTestRollbackOnUpdate());
        liquibase.setTag(this.properties.getTag());
        return liquibase;
    }

    private SpringLiquibase createSpringLiquibase(DataSource liquibaseDataSource, DataSource dataSource) {
        LiquibaseProperties properties = this.properties;
        DataSource migrationDataSource = getMigrationDataSource(liquibaseDataSource, dataSource, properties);
        SpringLiquibase liquibase = (migrationDataSource == liquibaseDataSource || migrationDataSource == dataSource) ? new SpringLiquibase() : new DataSourceClosingSpringLiquibase();
        liquibase.setDataSource(migrationDataSource);
        return liquibase;
    }

    private DataSource getMigrationDataSource(DataSource liquibaseDataSource, DataSource dataSource, LiquibaseProperties properties) {
        if (liquibaseDataSource != null) {
            return liquibaseDataSource;
        }
        if (properties.getUrl() != null) {
            DataSourceBuilder<?> builder = DataSourceBuilder.create().type(SimpleDriverDataSource.class);
            builder.url(properties.getUrl());
            applyCommonBuilderProperties(properties, builder);
            return builder.build();
        }
        if (properties.getUser() != null && dataSource != null) {
            DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource).type(SimpleDriverDataSource.class);
            applyCommonBuilderProperties(properties, builder);
            return builder.build();
        }
        Assert.state(dataSource != null, "Liquibase migration DataSource missing");
        return dataSource;
    }

    private void applyCommonBuilderProperties(LiquibaseProperties properties, DataSourceBuilder<?> builder) {
        builder.username(properties.getUser());
        builder.password(properties.getPassword());
        if (StringUtils.hasText(properties.getDriverClassName())) {
            builder.driverClassName(properties.getDriverClassName());
        }
    }

}
