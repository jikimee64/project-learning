package com.example.springbatch.learning.jdbc;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbcReader {

//    public JdbcCursorItemReader<Before> cursorItemReader(DataSource dataSource) {
//        String sql = "select documentId, username from BeforeEntity";
//        return new JdbcCursorItemReaderBuilder<Before>().name("beforeReader")
//            .dataSource(dataSource)
//            .sql(sql)
//            .rowMapper(new BeforeRowMapper())
//            .build();
//    }

//    @Bean
    public JdbcPagingItemReader<Before> pagingItemReader(DataSource dataSource) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("statusCode", "PE");
        parameterValues.put("type", "COLLECTION");

        return new JdbcPagingItemReaderBuilder<Before>().name("beforeReader")
            .dataSource(dataSource)
            .selectClause("select documentId, username")
            .fromClause("FROM BeforeEntity")
//            .whereClause("WHERE CREDIT > :credit")
            .sortKeys(Map.of("documentId", Order.ASCENDING))
            .rowMapper(new BeforeRowMapper())
            .pageSize(2)
            .parameterValues(parameterValues)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<After> itemWriter(DataSource dataSource) {
        String sql = "UPDATE BeforeEntity set credit = :credit where id = :id";
        return new JdbcBatchItemWriterBuilder<After>().dataSource(dataSource)
            .sql(sql)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()) // 파라미터 바인딩
            .assertUpdates(true) // 업데이트된 레코드 수 검증, 없으면 예외 발생
            .build();
    }

}
