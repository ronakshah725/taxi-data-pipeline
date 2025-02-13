package com.taxidata.batch;

import com.taxidata.config.ElasticsearchIndexConfig;
import jakarta.annotation.PostConstruct;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@TestConfiguration
@Profile("test")
public class TestConfig {
    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        return Mockito.mock(ElasticsearchOperations.class);
    }

    // This bean will replace the original ElasticsearchIndexConfig
    @Bean
    @Primary
    public ElasticsearchIndexConfig elasticsearchIndexConfig() {
        return new ElasticsearchIndexConfig(null) {
            @PostConstruct
            @Override
            public void setupIndex() {
                // Do nothing during tests
            }
        };
    }
}
