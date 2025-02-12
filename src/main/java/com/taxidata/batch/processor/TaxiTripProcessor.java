package com.taxidata.batch.processor;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.service.FailedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiTripProcessor implements ItemProcessor<TaxiTrip, TaxiTrip> {

    private final Validator validator;
    private final FailedRecordService failedRecordService;

    @Override
    public TaxiTrip process(TaxiTrip trip) {
        if (!isValidTrip(trip)) {
            return null;
        }

        try {
            return trip;
        } catch (Exception e) {
            log.error("Error processing taxi trip: {}", trip, e);
            failedRecordService.saveFailedRecord(trip, e.getMessage());
            return null;
        }
    }

    private boolean isValidTrip(TaxiTrip trip) {
        Set<ConstraintViolation<TaxiTrip>> violations = validator.validate(trip);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
                
            log.warn("Invalid taxi trip: {}", errorMessage);
            failedRecordService.saveFailedRecord(trip, errorMessage);
            return false;
        }

        if (!isValidTripDuration(trip)) {
            failedRecordService.saveFailedRecord(trip, "Invalid trip duration");
            return false;
        }

        if (!isValidAmount(trip)) {
            failedRecordService.saveFailedRecord(trip, "Invalid amount calculations");
            return false;
        }

        return true;
    }

    private boolean isValidTripDuration(TaxiTrip trip) {
        return trip.getPickupDatetime() != null 
            && trip.getDropoffDatetime() != null
            && !trip.getDropoffDatetime().isBefore(trip.getPickupDatetime());
    }

    private boolean isValidAmount(TaxiTrip trip) {
        return trip.getTotalAmount().compareTo(trip.getFareAmount()) >= 0;
    }
} 