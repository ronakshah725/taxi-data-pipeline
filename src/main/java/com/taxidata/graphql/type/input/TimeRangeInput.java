package com.taxidata.graphql.type.input;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeRangeInput {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}