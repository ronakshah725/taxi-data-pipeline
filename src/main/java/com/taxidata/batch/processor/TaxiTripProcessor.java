package com.taxidata.batch.processor;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.service.FailedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiTripProcessor implements ItemProcessor<TaxiTrip, TaxiTrip> {

    private final Validator validator;
    private final FailedRecordService failedRecordService;

    @Override
    public TaxiTrip process(TaxiTrip trip) throws Exception {
        log.debug("Processing trip with raw values: pickup={}, dropoff={}, passengers={}, distance={}, fare={}, total={}", 
            trip.getPickupDatetime(),
            trip.getDropoffDatetime(),
            trip.getPassengerCount(),
            trip.getTripDistance(),
            trip.getFareAmount(),
            trip.getTotalAmount()
        );

        Set<ConstraintViolation<TaxiTrip>> violations = validator.validate(trip);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
            log.error("Validation failed for trip: {}", errorMessage);
            failedRecordService.saveFailedRecord(trip, errorMessage);
            return null;
        }

        if (!isValidTripDuration(trip)) {
            failedRecordService.saveFailedRecord(trip, "Invalid trip duration");
            return null;
        }

        if (!isValidAmount(trip)) {
            failedRecordService.saveFailedRecord(trip, "Invalid amount calculations");
            return null;
        }

        return trip;
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