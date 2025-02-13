package com.taxidata.domain.document;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TripFilter {
    private TimeRange timeRange;
    private BigDecimal minFare;
    private BigDecimal maxFare;
    private Integer passengerCount;
}
