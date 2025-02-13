package com.taxidata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.taxidata.domain.document.TripDocument;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripElasticsearchRepository extends ElasticsearchRepository<TripDocument, String> {
    
    // Basic search with date range
    Page<TripDocument> findByPickupDatetimeBetween(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );

    @Query("""
        {
            "bool": {
                "must": [
                    {"range": {"pickupDatetime": {"gte": "?0", "lte": "?1"}}},
                    {"range": {"fareAmount": {"gte": "?2", "lte": "?3"}}}
                ]
            }
        }
    """)
    Page<TripDocument> searchTrips(
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal minFare,
        BigDecimal maxFare,
        Pageable pageable
    );

    // Stats queries don't need pagination
    @Query("""
        {
            "size": 0,
            "query": {
                "range": {
                    "pickupDatetime": {
                        "gte": "?0",
                        "lte": "?1"
                    }
                }
            },
            "aggs": {
                "avg_fare": {"avg": {"field": "fareAmount"}},
                "avg_distance": {"avg": {"field": "tripDistance"}},
                "total_trips": {"value_count": {"field": "_id"}}
            }
        }
    """)
    List<TripDocument> getTripStats(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
        {
            "size": 0,
            "query": {
                "range": {
                    "pickupDatetime": {
                        "gte": "?0",
                        "lte": "?1"
                    }
                }
            },
            "aggs": {
                "fare_stats": {
                    "stats": {"field": "fareAmount"}
                }
            }
        }
    """)
    List<TripDocument> getFareAnalysis(LocalDateTime startDate, LocalDateTime endDate);

    // Time-based trend analysis
    @Query("""
        {
            "size": 0,
            "query": {
                "range": {
                    "pickupDatetime": {
                        "gte": "?0",
                        "lte": "?1"
                    }
                }
            },
            "aggs": {
                "hourly_metrics": {
                    "date_histogram": {
                        "field": "pickupDatetime",
                        "calendar_interval": "hour"
                    },
                    "aggs": {
                        "avg_fare": {"avg": {"field": "fareAmount"}},
                        "total_rides": {"value_count": {"field": "_id"}},
                        "revenue": {"sum": {"field": "fareAmount"}},
                        "avg_distance": {"avg": {"field": "tripDistance"}}
                    }
                }
            }
        }
    """)
    List<TripDocument> getTrends(LocalDateTime startDate, LocalDateTime endDate);
}
