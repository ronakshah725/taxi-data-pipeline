package com.taxidata.config;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.taxidata.batch.processor.TaxiTripProcessor;
import com.taxidata.batch.reader.TaxiTripParquetReader;
import com.taxidata.domain.TaxiTrip;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BatchProperties.class)
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaxiTripParquetReader taxiTripReader;
    private final TaxiTripProcessor taxiTripProcessor;
    private final BatchProperties batchProperties;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job taxiDataImportJob() {
        return new JobBuilder(batchProperties.getJob().getName(), jobRepository)
            .start(importTaxiDataStep())
            .listener(new JobExecutionListener() {
                @Override
                public void beforeJob(JobExecution jobExecution) {
                    log.info("Starting job: {}", batchProperties.getJob().getName());
                }

                @Override
                public void afterJob(JobExecution jobExecution) {
                    log.info("Job complete: {}. Status: {}", 
                        batchProperties.getJob().getName(),
                        jobExecution.getStatus());
                }
            })
            .build();
    }

    @Bean
    public Step importTaxiDataStep() {
        return new StepBuilder(batchProperties.getJob().getStep().getName(), jobRepository)
            .<TaxiTrip, TaxiTrip>chunk(batchProperties.getJob().getChunkSize(), transactionManager)
            .reader(taxiTripReader)
            .processor(taxiTripProcessor)
            .writer(taxiTripWriter())
            .faultTolerant()
            .retryLimit(batchProperties.getJob().getRetry().getLimit())
            .retry(Exception.class)
            // .skipLimit(10000)
            // .skip(DuplicateKeyException.class)
            // .skip(ConstraintViolationException.class)
            // .noRollback(DuplicateKeyException.class)
            // .noRollback(ConstraintViolationException.class)
            .allowStartIfComplete(true)
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("Starting step: {}", stepExecution.getStepName());
                }

                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("Step complete: {}. Read count: {}, Skip count: {}", 
                        stepExecution.getStepName(),
                        stepExecution.getReadCount(),
                        stepExecution.getSkipCount());
                    return ExitStatus.COMPLETED;
                }
            })
            .build();
    }

    // TODO move to a separate class
    @Bean
    public JpaItemWriter<TaxiTrip> taxiTripWriter() {
        JpaItemWriter<TaxiTrip> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        try {
            writer.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JpaItemWriter", e);
        }
        return writer;
    }
} 