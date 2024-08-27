package com.example.springbatch.config;

import java.util.Collection;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({JpaProperties.class, HibernateProperties.class})
public class TransactionManagerConfig {
    public static final String DOMAIN_ENTITY_MANAGER_FACTORY = "domainEntityManagerFactory";
    public static final String META_TRANSACTION_MANAGER = "metaTransactionManager";
    public static final String DOMAIN_TRANSACTION_MANAGER = "domainTransactionManager";

    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;
    private final ObjectProvider<Collection<DataSourcePoolMetadataProvider>> metadataProviders;
    private final EntityManagerFactoryBuilder entityManagerFactoryBuilder;


    @Bean(name = META_TRANSACTION_MANAGER)
    public PlatformTransactionManager metaTransactionManager(
        @Qualifier(DataSourceConfig.META_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean(name = DOMAIN_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean domainEntityManagerFactory(@Qualifier(DataSourceConfig.DOMAIN_DATASOURCE) DataSource dataSource)  {
        return EntityManagerFactoryCreator.builder()
            .properties(jpaProperties)
            .hibernateProperties(hibernateProperties)
            .metadataProviders(metadataProviders)
            .entityManagerFactoryBuilder(entityManagerFactoryBuilder)
            .dataSource(dataSource)
            .packages("com.example.springbatch.entity")
            .persistenceUnit("domainUnit")
            .build()
            .create();
    }

    @Bean(name = DOMAIN_TRANSACTION_MANAGER)
    public PlatformTransactionManager domainTransactionManager(@Qualifier(DOMAIN_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = "com.example.springbatch.repository"
        ,entityManagerFactoryRef = DOMAIN_ENTITY_MANAGER_FACTORY
        ,transactionManagerRef = DOMAIN_TRANSACTION_MANAGER
    )
    public static class DomainJpaRepositoriesConfig{}
}
