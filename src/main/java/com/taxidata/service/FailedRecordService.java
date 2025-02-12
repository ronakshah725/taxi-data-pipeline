package com.taxidata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxidata.domain.FailedRecord;
import com.taxidata.domain.TaxiTrip;
import com.taxidata.repository.FailedRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FailedRecordService {
    
    private final FailedRecordRepository failedRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveFailedRecord(TaxiTrip trip, String errorMessage) {
        try {
            String recordData = objectMapper.writeValueAsString(trip);
            String recordId = generateRecordId(trip);
            
            failedRecordRepository.findById(Long.parseLong(recordId))
                .ifPresentOrElse(
                    // Update existing record
                    existing -> {
                        existing.setRecordData(recordData);
                        existing.setErrorMessage(errorMessage);
                        failedRecordRepository.save(existing);
                    },
                    // Create new record
                    () -> {
                        FailedRecord failedRecord = FailedRecord.builder()
                            .recordId(recordId)
                            .recordData(recordData)
                            .errorMessage(errorMessage)
                            .createdAt(LocalDateTime.now())
                            .build();
                        failedRecordRepository.save(failedRecord);
                    }
                );
            log.debug("Saved failed record: {}", recordId);
        } catch (Exception e) {
            log.error("Error saving failed record for trip: {}", trip, e);
        }
    }

    private String generateRecordId(TaxiTrip trip) {
        return String.format("%s_%d",
            trip.getPickupDatetime().toLocalDate(),
            System.currentTimeMillis());
    }
}