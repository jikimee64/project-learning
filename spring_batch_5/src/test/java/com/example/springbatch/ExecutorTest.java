package com.example.springbatch;

import static com.example.springbatch.batch.migration.MigrationJob.MIGRATION_JOB_NAME;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ExecutorTest {

    @Autowired
    @Qualifier(MIGRATION_JOB_NAME)
    private Job migrationJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Test
    void test() throws Exception{
        JobParameters jobParameters = new JobParametersBuilder()
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(migrationJob, jobParameters);
    }

}
