package com.taxidata.batch.processor;

import com.taxidata.batch.ingest.TaxiTripProcessor;
import com.taxidata.domain.TaxiTrip;
import com.taxidata.service.FailedRecordService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class TaxiTripProcessorTest {

    private TaxiTripProcessor processor;
    private Validator validator;
    
    @Mock
    private FailedRecordService failedRecordService;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        processor = new TaxiTripProcessor(validator, failedRecordService);
    }

    @Test
    void shouldProcessValidTrip() throws Exception {
        TaxiTrip trip = createValidTrip();
        
        TaxiTrip processed = processor.process(trip);
        
        assertThat(processed).isNotNull();
        assertThat(processed.getPassengerCount()).isPositive();
        assertThat(processed.getTripDistance()).isPositive();
    }

    @Test
    void shouldRejectInvalidTrip() throws Exception {
        TaxiTrip trip = createInvalidTrip();
        
        TaxiTrip processed = processor.process(trip);
        
        assertThat(processed).isNull();
        verify(failedRecordService).saveFailedRecord(any(), any());
    }

    private TaxiTrip createValidTrip() {
        return TaxiTrip.builder()
            .pickupDatetime(LocalDateTime.now().minusHours(1))
            .dropoffDatetime(LocalDateTime.now())
            .passengerCount(2)
            .tripDistance(new BigDecimal("5.5"))
            .fareAmount(new BigDecimal("20.0"))
            .totalAmount(new BigDecimal("25.0"))
            .build();
    }
    
    private TaxiTrip createInvalidTrip() {
        // No passenger count
        return TaxiTrip.builder()
            .pickupDatetime(LocalDateTime.now())
            .dropoffDatetime(LocalDateTime.now().minusHours(1))
            .tripDistance(BigDecimal.ZERO)
            .fareAmount(new BigDecimal("20.0"))
            .totalAmount(new BigDecimal("10.0"))
            .build();
    }
} 