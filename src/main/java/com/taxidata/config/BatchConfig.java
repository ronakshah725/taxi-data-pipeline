package com.taxidata.config;

import com.taxidata.batch.processor.TaxiTripProcessor;
import com.taxidata.batch.reader.TaxiTripParquetReader;
import com.taxidata.domain.TaxiTrip;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
            .writer(loggingWriter())
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

    @Bean
    public ItemWriter<TaxiTrip> loggingWriter() {
        return items -> {
            for (TaxiTrip trip : items) {
                log.debug("Processing trip: {}", trip);
            }
            log.info("Processed {} items", items.size());
        };
    }
} 