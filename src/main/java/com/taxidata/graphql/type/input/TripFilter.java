package com.taxidata.graphql.type.input;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TripFilter {
    private TimeRangeInput timeRange;
    private BigDecimal minFare;
    private BigDecimal maxFare;
    private Integer passengerCount;
}