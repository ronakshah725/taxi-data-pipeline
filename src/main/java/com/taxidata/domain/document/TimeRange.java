package com.taxidata.domain.document;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeRange {
    private LocalDateTime start;
    private LocalDateTime end;
}
