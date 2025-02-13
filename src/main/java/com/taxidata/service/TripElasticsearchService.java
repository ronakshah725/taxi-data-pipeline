package com.taxidata.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.graphql.type.PaymentAnalytics;
import com.taxidata.graphql.type.PaymentTypeCount;
import com.taxidata.graphql.type.PaymentTypeFare;
import com.taxidata.graphql.type.VendorMetric;
import com.taxidata.graphql.type.VendorStats;
import com.taxidata.repository.TripElasticsearchRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TripElasticsearchService {
    private final TripElasticsearchRepository searchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public long getDocumentCount() {
        return searchRepository.count();
    }

    public LocalDateTime getLastSyncedTime() {
        Page<TripDocument> lastDoc = searchRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
        return lastDoc.hasContent() ? lastDoc.getContent().get(0).getLastModifiedDate() : null;
    }

    public Page<TripDocument> searchTrips(
            Integer vendorId,
            Integer paymentType,
            BigDecimal minFare,
            BigDecimal maxFare,
            Integer passengerCount,
            String sortField,
            Sort.Direction direction,
            int page,
            int size) {

        return searchRepository.searchTrips(
                vendorId, paymentType, minFare, maxFare, passengerCount,
                PageRequest.of(page, size, Sort.by(direction, sortField)));
    }

    public VendorStats getVendorStats() {
        SearchHits<TripDocument> searchHits = searchRepository.getVendorStats();
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();

        if (aggs == null || aggs.aggregationsAsMap().get("vendors") == null) {
            log.warn("No vendor aggregations found");
            return VendorStats.empty();
        }

        List<StringTermsBucket> buckets = aggs.aggregationsAsMap().get("vendors").aggregation()
                .getAggregate().sterms().buckets().array().stream()
                .collect(Collectors.toList());

        List<VendorMetric> metrics = buckets.stream()
                .map(bucket -> new VendorMetric(
                        Integer.parseInt(bucket.key().stringValue()),
                        (int) bucket.docCount(),
                        BigDecimal.valueOf(bucket.aggregations().get("avg_fare").avg().value()),
                        BigDecimal.valueOf(bucket.aggregations().get("total_revenue").sum().value()),
                        bucket.aggregations().get("avg_passengers").avg().value()))
                .collect(Collectors.toList());

        return new VendorStats(metrics);
    }

    public PaymentAnalytics getPaymentAnalytics() {
        SearchHits<TripDocument> searchHits = searchRepository.getPaymentAnalytics();
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();

        if (aggs == null || aggs.aggregationsAsMap().get("payment_types") == null) {
            log.warn("No payment aggregations found");
            return new PaymentAnalytics(List.of(), List.of());
        }

        List<StringTermsBucket> buckets = aggs.aggregationsAsMap().get("payment_types").aggregation()
                .getAggregate().sterms().buckets().array().stream()
                .collect(Collectors.toList());

        List<PaymentTypeCount> counts = buckets.stream()
                .map(bucket -> new PaymentTypeCount(
                        Integer.parseInt(bucket.key().stringValue()),
                        (int) bucket.docCount(),
                        BigDecimal.valueOf(bucket.aggregations().get("total_revenue").sum().value())))
                .collect(Collectors.toList());

        List<PaymentTypeFare> fares = buckets.stream()
                .map(bucket -> new PaymentTypeFare(
                        Integer.parseInt(bucket.key().stringValue()),
                        BigDecimal.valueOf(bucket.aggregations().get("avg_fare").avg().value())))
                .collect(Collectors.toList());

        return new PaymentAnalytics(counts, fares);
    }

}