package com.taxidata.controller;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.service.TripElasticsearchService;
import com.taxidata.service.JobMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final TripElasticsearchService searchService;
    private final JobMetricsService jobMetricsService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("documentCount", searchService.getDocumentCount());
        status.put("lastSyncedAt", searchService.getLastSyncedTime());
        status.put("lastJobExecution", jobMetricsService.getLastJobExecution());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/trips")
    public ResponseEntity<Page<TripDocument>> searchTrips(
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Integer paymentType,
            @RequestParam(defaultValue = "0") BigDecimal minFare,
            @RequestParam(defaultValue = "1000000") BigDecimal maxFare,
            @RequestParam(defaultValue = "fareAmount") String sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        
        return ResponseEntity.ok(searchService.searchTrips(
            vendorId, paymentType, minFare, maxFare,
            sortField, sortDir, page, size));
    }
} 