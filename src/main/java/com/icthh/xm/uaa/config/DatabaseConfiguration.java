package com.icthh.xm.uaa.config;

import static com.icthh.xm.uaa.config.Constants.CHANGE_LOG_PATH;
import static com.icthh.xm.uaa.config.Constants.DB_SCHEMA_CREATETION_ENABLED;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.icthh.xm.commons.config.client.repository.TenantListRepository;
import com.icthh.xm.commons.migration.db.XmMultiTenantSpringLiquibase;
import com.icthh.xm.commons.migration.db.XmSpringLiquibase;

import com.icthh.xm.uaa.util.DatabaseUtil;
import io.github.jhipster.config.JHipsterConstants;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import liquibase.integration.spring.MultiTenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang.StringUtils;
import org.h2.tools.Server;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("com.icthh.xm.uaa.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private static final String JPA_PACKAGES = "com.icthh.xm.uaa.domain";

    private final Environment env;
    private final JpaProperties jpaProperties;
    private final TenantListRepository tenantListRepository;
    private ApplicationProperties applicationProperties;

    public DatabaseConfiguration(Environment env,
                                 JpaProperties jpaProperties,
                                 TenantListRepository tenantListRepository,
                                 ApplicationProperties applicationProperties) {
        this.env = env;
        this.jpaProperties = jpaProperties;
        this.tenantListRepository = tenantListRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server
     * @throws SQLException if the server failed to start
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public Server h2TCPServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers");
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties liquibaseProperties) {
        createSchemas(dataSource);
        SpringLiquibase liquibase = new XmSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(CHANGE_LOG_PATH);
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }
        liquibase.setChangeLogParameters(DatabaseUtil.defaultParams(liquibaseProperties.getDefaultSchema()));
        return liquibase;
    }

    @Bean
    @DependsOn("liquibase")
    public MultiTenantSpringLiquibase multiTenantLiquibase(
        DataSource dataSource,
        LiquibaseProperties liquibaseProperties) {
        MultiTenantSpringLiquibase liquibase = new XmMultiTenantSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(CHANGE_LOG_PATH);
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setSchemas(getSchemas());
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }
        liquibase.setParameters(DatabaseUtil.defaultParams(liquibaseProperties.getDefaultSchema()));
        return liquibase;
    }

    private void createSchemas(DataSource dataSource) {
        if (jpaProperties.getProperties().containsKey(DB_SCHEMA_CREATETION_ENABLED)
            && !Boolean.valueOf(jpaProperties.getProperties().get(DB_SCHEMA_CREATETION_ENABLED))) {
            log.info("Schema creation for {} jpa provider is disabled", jpaProperties.getDatabase());
            return;
        }
        for (String schema : getSchemas()) {
            try {
                DatabaseUtil.createSchema(dataSource, schema);
            } catch (Exception e) {
                log.error("Failed to create schema '{}', error: {}", schema, e.getMessage(), e);
            }
        }
    }

    private List<String> getSchemas() {
        String suffix = applicationProperties.getDbSchemaSuffix();
        List<String> schemas = new ArrayList<>(tenantListRepository.getTenants());

        if (StringUtils.isNotBlank(suffix)) {
            return schemas.stream().map((schema) -> (schema.concat(suffix)))
                .collect(Collectors.toList());
        }
        return schemas;
    }

    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        DataSource dataSource,
        MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
        CurrentTenantIdentifierResolver currentTenantIdentifierResolverImpl) {
        Map<String, Object> properties = new HashMap<>();
        properties.putAll(jpaProperties.getHibernateProperties(new HibernateSettings()));
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        properties
            .put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
        properties
            .put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(JPA_PACKAGES);
        em.setJpaVendorAdapter(jpaVendorAdapter());
        em.setJpaPropertyMap(properties);
        return em;
    }
}
