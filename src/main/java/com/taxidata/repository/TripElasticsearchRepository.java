package com.taxidata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
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
            {"term": {"vendorId": "?0"}},
            {"term": {"paymentType": "?1"}},
            {"range": {"fareAmount": {"gte": "?2", "lte": "?3"}}},
            {"term": {"passengerCount": "?4"}}
          ]
        }
      }
      """)
  Page<TripDocument> searchTrips(
      Integer vendorId,
      Integer paymentType,
      BigDecimal minFare,
      BigDecimal maxFare,
      Integer passengerCount,
      Pageable pageable);

  @Query("""
      {
        "size": 0,
        "aggs": {
          "payment_types": {
            "terms": {
              "field": "paymentType"
            },
            "aggs": {
              "total_revenue": {
                "sum": {"field": "totalAmount"}
              },
              "avg_fare": {
                "avg": {"field": "fareAmount"}
              }
            }
          }
        }
      }
      """)
  SearchHits<TripDocument> getPaymentAnalytics();

  // @Query("""
  // {
  // "size": 0,
  // "aggs": {
  // "vendors": {
  // "terms": {
  // "field": "vendorId"
  // },
  // "aggs": {
  // "total_revenue": {
  // "sum": {"field": "totalAmount"}
  // },
  // "avg_fare": {
  // "avg": {"field": "fareAmount"}
  // },
  // "avg_passengers": {
  // "avg": {"field": "passengerCount"}
  // }
  // }
  // }
  // }
  // }
  // """)
  @Query("""
      {
        "size": 0,
        "aggs": {
          "vendors": {
            "terms": {
              "field": "vendorId",
              "size": 10
            },
            "aggs": {
              "total_revenue": {
                "sum": {
                  "field": "totalAmount"
                }
              },
              "avg_fare": {
                "avg": {
                  "field": "fareAmount"
                }
              },
              "avg_passengers": {
                "avg": {
                  "field": "passengerCount"
                }
              }
            }
          }
        }
      }
      """)
  SearchHits<TripDocument> getVendorStats();

}
