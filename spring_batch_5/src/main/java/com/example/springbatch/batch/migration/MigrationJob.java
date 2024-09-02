package com.example.springbatch.batch.migration;

import com.example.springbatch.entity.AfterEntity;
import com.example.springbatch.entity.BeforeEntity;
import com.example.springbatch.repository.mysql.AfterRepository;
import com.example.springbatch.repository.mssql.BeforeRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MigrationJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final BeforeRepository beforeRepository;
    private final AfterRepository afterRepository;

    public static final int CHUNK_SIZE = 2;
    public static final String MIGRATION_JOB_NAME = "MIGRATION_JOB";
    public static final String STEP1_NAME = "FIRST_STEP";

    @Bean(name = MIGRATION_JOB_NAME)
    public Job job(){
        System.out.println("first job");
        return new JobBuilder(MIGRATION_JOB_NAME, jobRepository)
            .start(firstStep())
            .build();
    }

    @Bean
    public Step firstStep(){
        System.out.println("first step");
        return new StepBuilder(STEP1_NAME, jobRepository)
            .<BeforeEntity, AfterEntity>chunk(CHUNK_SIZE, platformTransactionManager)
            .reader(beforeReader())
            .processor(middleProcessor())
            .writer(afterWriter())
            .build();
    }

    // jpa를 사용하므로 RepositoryItemReader 사용
    @Bean
    public RepositoryItemReader<BeforeEntity> beforeReader() {
        return new RepositoryItemReaderBuilder<BeforeEntity>()
            .name("beforeReader")
            .pageSize(10)
            .methodName("findAll") // 청크 단위까지만 읽기 때문에 findAll을 하더라도 chunk 개수 만큼 사용
            .repository(beforeRepository)
            .sorts(Collections.singletonMap("documentId", Sort.Direction.ASC))
            .build();
    }

    @Bean
    public ItemProcessor<BeforeEntity, AfterEntity> middleProcessor() {
        return beforeEntity -> {
            System.out.println("middle processor");
            return new AfterEntity(beforeEntity.getDocumentId(), beforeEntity.getUsername());
        };
    }

    // jpa를 사용하므로 RepositoryItemWriter 사용
    @Bean
    public RepositoryItemWriter<AfterEntity> afterWriter() {
        return new RepositoryItemWriterBuilder<AfterEntity>()
            .repository(afterRepository)
            .methodName("save")
            .build();
    }

}
