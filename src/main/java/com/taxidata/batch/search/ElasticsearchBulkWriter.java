package com.taxidata.batch.search;

import com.taxidata.domain.document.TripDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchBulkWriter implements ItemWriter<TripDocument> {

    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "taxi-trips";
    private static final int BATCH_SIZE = 1000;

    @Override
    public void write(Chunk<? extends TripDocument> chunk) {
        List<IndexQuery> queries = chunk.getItems().stream()
                .map(document -> new IndexQueryBuilder()
                        .withId(document.getId())
                        .withObject(document)
                        .build())
                .collect(Collectors.toList());

        // Split into smaller batches for optimal performance
        for (int i = 0; i < queries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, queries.size());
            List<IndexQuery> batch = queries.subList(i, end);
            
            try {
                elasticsearchOperations.bulkIndex(
                    batch,
                    IndexCoordinates.of(INDEX_NAME)
                );
                log.debug("Indexed batch of {} documents", batch.size());
            } catch (Exception e) {
                log.error("Error indexing batch: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to index documents", e);
            }
        }
    }
}
