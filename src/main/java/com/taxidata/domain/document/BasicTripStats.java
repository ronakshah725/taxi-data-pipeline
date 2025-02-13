package com.taxidata.domain.document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicTripStats {
    private long totalTrips;
    private double averageFare;
    private double maxFare;
    private double minFare;
}
