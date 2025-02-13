package com.taxidata.batch.search;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.domain.document.TripDocument;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexingConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TripDocumentProcessor tripDocumentProcessor;
    private final ElasticsearchBulkWriter elasticsearchBulkWriter;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job elasticsearchIndexingJob() {
        return new JobBuilder("elasticsearchIndexingJob", jobRepository)
                .start(elasticsearchIndexingStep())
                .build();
    }

    @Bean
    public Step elasticsearchIndexingStep() {
        return new StepBuilder("elasticsearchIndexingStep", jobRepository)
                .<TaxiTrip, TripDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(tripReader(null))
                .processor(tripDocumentProcessor)
                .writer(elasticsearchBulkWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TaxiTrip> tripReader(
            @Value("#{jobParameters['lastSync']}") String lastSyncStr) {
        
        LocalDateTime lastSync = lastSyncStr != null 
            ? LocalDateTime.parse(lastSyncStr)
            : LocalDateTime.now().minusDays(1);

        log.info("Starting indexing from: {}", lastSync);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lastSync", lastSync);

        return new JpaPagingItemReaderBuilder<TaxiTrip>()
                .name("tripReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT t FROM TaxiTrip t WHERE t.updatedAt > :lastSync ORDER BY t.id")
                .parameterValues(parameters)
                .build();
    }
}
