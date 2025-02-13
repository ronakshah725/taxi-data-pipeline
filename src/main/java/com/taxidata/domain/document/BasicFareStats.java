package com.taxidata.domain.document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicFareStats {
    private double averageFare;
    private double totalRevenue;
    private long tripCount;
}
