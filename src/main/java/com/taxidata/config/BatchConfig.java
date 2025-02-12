package com.taxidata.config;

import com.taxidata.batch.reader.TaxiTripParquetReader;
import com.taxidata.domain.TaxiTrip;
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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaxiTripParquetReader taxiTripReader;

    @Bean
    public Job taxiDataImportJob() {
        return new JobBuilder("taxiDataImportJob", jobRepository)
            .start(importTaxiDataStep())
            .build();
    }

    @Bean
    public Step importTaxiDataStep() {
        return new StepBuilder("importTaxiDataStep", jobRepository)
            .<TaxiTrip, TaxiTrip>chunk(1000, transactionManager)
            .reader(taxiTripReader)
            .writer(loggingWriter())
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