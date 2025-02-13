package com.taxidata.graphql.resolver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.graphql.type.PaymentAnalytics;
import com.taxidata.graphql.type.TripInputDto.PageInput;
import com.taxidata.graphql.type.TripInputDto.TripFilter;
import com.taxidata.graphql.type.TripInputDto.TripSort;
import com.taxidata.graphql.type.VendorStats;
import com.taxidata.service.JobMetricsService;
import com.taxidata.service.TripElasticsearchService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TripQueryResolver {
    private final TripElasticsearchService searchService;
    private final JobMetricsService jobMetricsService;

    @QueryMapping
    public Map<String, Object> indexStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("documentCount", searchService.getDocumentCount());
        status.put("lastSyncedAt", searchService.getLastSyncedTime());
        status.put("lastJobExecution", jobMetricsService.getLastJobExecution());
        return status;
    }


    @QueryMapping
    public Page<TripDocument> searchTrips(
            @Argument TripFilter filter,
            @Argument TripSort sort,
            @Argument PageInput pageInput
    ) {
        if (pageInput == null) {
            pageInput = new PageInput(0, 20);
        }

        Sort.Direction direction = sort != null ? sort.getSpringDirection() : Sort.Direction.DESC;
        String sortField = sort != null ? sort.getField().toString().toLowerCase() : "fareAmount";

        return searchService.searchTrips(
            filter.getVendorId(),
            filter.getPaymentType(),
            filter.getMinFare(),
            filter.getMaxFare(),
            filter.getPassengerCount(),
            sortField,
            direction,
            pageInput.getPage(),
            pageInput.getSize()
        );
    }

    @QueryMapping
    public PaymentAnalytics paymentAnalytics() {
        return searchService.getPaymentAnalytics();
    }

    @QueryMapping
    public VendorStats vendorStats() {
        return searchService.getVendorStats();
    }
}