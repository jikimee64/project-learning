package com.example.springbatch.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
public class DataSourceConfig {
    public static final String MYSQL_DATASOURCE = "mysqlDataSource";
    public static final String MSSQL_DATASOURCE = "mssqlDataSource";

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.mysql.hikari")
    public HikariConfig mysqlHikariConfig() {
        return new HikariConfig();
    }

    @Primary
    @Bean(MYSQL_DATASOURCE)
    public DataSource mysqlDataSource() {
        return new LazyConnectionDataSourceProxy(new HikariDataSource(mysqlHikariConfig()));
    }

//

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.mssql.hikari")
    public HikariConfig mssqlHikariConfig() {
        return new HikariConfig();
    }

    @Bean(MSSQL_DATASOURCE)
    public DataSource mssqlDataSource() {
        return new LazyConnectionDataSourceProxy(new HikariDataSource(mssqlHikariConfig()));
    }
}