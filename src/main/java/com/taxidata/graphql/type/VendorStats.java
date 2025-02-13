package com.taxidata.graphql.type;

import java.util.Collections;
import java.util.List;
import lombok.Value;

@Value
public class VendorStats {
    List<VendorMetric> vendorMetrics;

    public static VendorStats empty() {
        return new VendorStats(Collections.emptyList());
    }
}