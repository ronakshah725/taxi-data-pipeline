package com.taxidata.config;

import com.taxidata.domain.document.TripDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {

    private final ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void setupIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(TripDocument.class);
            
            if (!indexOps.exists()) {
                log.info("Creating index for taxi trips");
                indexOps.create();
                indexOps.putMapping();
                log.info("Successfully created index with mappings");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index", e);
            throw new RuntimeException("Failed to initialize Elasticsearch index", e);
        }
    }
}
