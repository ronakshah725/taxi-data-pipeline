package com.taxidata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.taxidata.domain.document.TripDocument;
import java.math.BigDecimal;

@Repository
public interface TripElasticsearchRepository extends ElasticsearchRepository<TripDocument, String> {
    
    @Query("""
        {
            "bool": {
                "must": [
                    {"range": {"fareAmount": {"gte": "?2", "lte": "?3"}}},
                    {"range": {"vendorId": {"gte": "?0", "lte": "?2"}}},
                    {"range": {"paymentType": {"gte": "?1", "lte": "?6"}}}
                ]
            }
        }
    """)
    Page<TripDocument> searchTrips(
        Integer vendorId,
        Integer paymentType,
        BigDecimal minFare,
        BigDecimal maxFare,
        Pageable pageable
    );
    

    // // Stats queries don't need pagination
    // @Query("""
    //     {
    //         "size": 0,
    //         "query": {
    //             "range": {
    //                 "pickupDatetime": {
    //                     "gte": "?0",
    //                     "lte": "?1"
    //                 }
    //             }
    //         },
    //         "aggs": {
    //             "avg_fare": {"avg": {"field": "fareAmount"}},
    //             "avg_distance": {"avg": {"field": "tripDistance"}},
    //             "total_trips": {"value_count": {"field": "_id"}}
    //         }
    //     }
    // """)
    // List<TripDocument> getTripStats(LocalDateTime startDate, LocalDateTime endDate);


}
