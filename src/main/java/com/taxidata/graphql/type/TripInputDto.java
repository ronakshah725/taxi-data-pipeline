package com.taxidata.graphql.type;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Sort;

public class TripInputDto {

    @Data
    public static class TripFilter {
        private Integer vendorId;
        private Integer paymentType;
        private BigDecimal minFare;
        private BigDecimal maxFare;
        private Integer passengerCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageInput {
        private int page;
        private int size;
    }

    @Data
    public static class TripSort {
        private TripSortField field;
        private SortDirection direction;

        public Sort.Direction getSpringDirection() {
            return direction.toSpringDirection();
        }
    }

    public enum TripSortField {
        FARE_AMOUNT,
        TRIP_DISTANCE,
        TOTAL_AMOUNT,
        PASSENGER_COUNT;
    }

    public enum SortDirection {
        ASC {
            @Override
            public Sort.Direction toSpringDirection() {
                return Sort.Direction.ASC;
            }
        },
        DESC {
            @Override
            public Sort.Direction toSpringDirection() {
                return Sort.Direction.DESC;
            }
        };

        public abstract Sort.Direction toSpringDirection();
    }
}