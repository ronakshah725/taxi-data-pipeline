package com.taxidata.service;

import com.taxidata.domain.document.TripDocument;
import com.taxidata.repository.TripElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastModifiedDate"))
        );
        return lastDoc.hasContent() ? lastDoc.getContent().get(0).getLastModifiedDate() : null;
    }

    public Page<TripDocument> searchTrips(
            Integer vendorId,
            Integer paymentType,
            BigDecimal minFare,
            BigDecimal maxFare,
            String sortField,
            Sort.Direction direction,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return searchRepository.searchTrips(vendorId, paymentType, minFare, maxFare, pageable);
    }
}
