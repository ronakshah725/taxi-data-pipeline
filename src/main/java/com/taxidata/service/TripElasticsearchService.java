package com.taxidata.service;

import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.taxidata.domain.document.TripDocument;
import com.taxidata.graphql.type.*;
import com.taxidata.repository.TripElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TripElasticsearchService {
    private final TripElasticsearchRepository searchRepository;

    public long getDocumentCount() {
        return searchRepository.count();
    }

    public LocalDateTime getLastSyncedTime() {
        Page<TripDocument> lastDoc = searchRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
        return lastDoc.hasContent() ? lastDoc.getContent().getFirst().getLastModifiedDate() : null;
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
                .toList();

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
                .toList();

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