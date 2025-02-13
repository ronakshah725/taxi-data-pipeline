package com.taxidata.service;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.repository.TaxiTripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxiTripService {
    
    private final TaxiTripRepository taxiTripRepository;

    @Transactional(readOnly = true)
    public List<TaxiTrip> findTripsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trips between {} and {}", startDate, endDate);
        return taxiTripRepository.findTripsInDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public long countTripsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return taxiTripRepository.countTripsInDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Double calculateAverageFare(LocalDateTime startDate, LocalDateTime endDate) {
        return taxiTripRepository.calculateAverageFare(startDate, endDate);
    }

    @Transactional
    public List<TaxiTrip> saveAll(List<TaxiTrip> trips) {
        log.info("Saving batch of {} trips", trips.size());
        return taxiTripRepository.saveAll(trips);
    }
}