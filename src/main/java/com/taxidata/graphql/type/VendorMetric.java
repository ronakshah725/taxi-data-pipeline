package com.taxidata.graphql.type;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class VendorMetric {
    int vendorId;
    int totalTrips;
    BigDecimal averageFare;
    BigDecimal totalRevenue;
    double averagePassengers;
}