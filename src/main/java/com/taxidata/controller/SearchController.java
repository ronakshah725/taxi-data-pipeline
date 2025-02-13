package com.taxidata.controller;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.service.TripElasticsearchService;
import com.taxidata.service.JobMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minFare,
            @RequestParam(required = false) BigDecimal maxFare,
            @RequestParam(defaultValue = "pickupDatetime") String sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Default to the start of time if startDate is not provided
        if (startDate == null) {
            startDate = LocalDateTime.of(1970, 1, 1, 0, 0);
        }

        // Default to the current date and time if endDate is not provided
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.debug("Searching trips between {} and {}", startDate, endDate);
        
        if (minFare != null && maxFare != null) {
            return ResponseEntity.ok(searchService.searchTrips(
                startDate, endDate, minFare, maxFare,
                sortField, sortDir, page, size));
        }

        return ResponseEntity.ok(searchService.findTrips(
            startDate, endDate,
            org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortDir, sortField))));
    }
} 