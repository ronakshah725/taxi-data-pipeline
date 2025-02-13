package com.taxidata.graphql.resolver;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.graphql.type.input.TripFilter;
import com.taxidata.graphql.type.input.PageInput;
import com.taxidata.service.TripElasticsearchService;
import com.taxidata.service.JobMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.HashMap;

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
            @Argument PageInput pageInput
    ) {
        

        if (filter == null) {
            filter = new TripFilter();
        }

        if (pageInput == null) {
            pageInput = new PageInput(0, 20);
        }

        return searchService.searchTrips(
            filter.getVendorId(),
            filter.getPaymentType(),
            filter.getMinFare(),
            filter.getMaxFare(),
            "fareAmount",
            Sort.Direction.DESC,
            pageInput.getPage(),
            pageInput.getSize()
        );
    }
}