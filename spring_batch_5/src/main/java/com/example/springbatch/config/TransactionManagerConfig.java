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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({JpaProperties.class, HibernateProperties.class})
public class TransactionManagerConfig {
    public static final String MSSQL_TRANSACTION_MANAGER = "mssqlTransactionManager";
    public static final String MSSQL_ENTITY_MANAGER_FACTORY = "mssqlEntityManagerFactory";

    public static final String MYSQL_TRANSACTION_MANAGER = "mysqlTransactionManager";
    public static final String MYSQL_ENTITY_MANAGER_FACTORY = "mysqlEntityManagerFactory";

    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;
    private final ObjectProvider<Collection<DataSourcePoolMetadataProvider>> metadataProviders;
    private final EntityManagerFactoryBuilder entityManagerFactoryBuilder;

    @Primary
    @Bean(name = MYSQL_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(@Qualifier(DataSourceConfig.MYSQL_DATASOURCE) DataSource dataSource)  {
        return EntityManagerFactoryCreator.builder()
            .properties(jpaProperties)
            .hibernateProperties(hibernateProperties)
            .metadataProviders(metadataProviders)
            .entityManagerFactoryBuilder(entityManagerFactoryBuilder)
            .dataSource(dataSource)
            .packages("com.example.springbatch.entity")
            .persistenceUnit("mysqlUnit")
            .build()
            .create();
    }

    @Primary
    @Bean(name = MYSQL_TRANSACTION_MANAGER)
    public PlatformTransactionManager mysqlTransactionManager(@Qualifier(MYSQL_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = "com.example.springbatch.repository.mysql"
        ,entityManagerFactoryRef = MYSQL_ENTITY_MANAGER_FACTORY
        ,transactionManagerRef = MYSQL_TRANSACTION_MANAGER
    )
    public static class MysqlJpaRepositoriesConfig{}

//    ====

    @Bean(name = MSSQL_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean mssqlEntityManagerFactory(@Qualifier(DataSourceConfig.MSSQL_DATASOURCE) DataSource dataSource)  {
        return EntityManagerFactoryCreator.builder()
            .properties(jpaProperties)
            .hibernateProperties(hibernateProperties)
            .metadataProviders(metadataProviders)
            .entityManagerFactoryBuilder(entityManagerFactoryBuilder)
            .dataSource(dataSource)
            .packages("com.example.springbatch.entity")
            .persistenceUnit("mssqlUnit")
            .build()
            .create();
    }

    @Bean(name = MSSQL_TRANSACTION_MANAGER)
    public PlatformTransactionManager mssqlTransactionManager(@Qualifier(MSSQL_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = "com.example.springbatch.repository.mssql"
        ,entityManagerFactoryRef = MSSQL_ENTITY_MANAGER_FACTORY
        ,transactionManagerRef = MSSQL_TRANSACTION_MANAGER
    )
    public static class MssqlJpaRepositoriesConfig{}
}
