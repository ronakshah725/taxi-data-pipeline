package com.taxidata.repository;

import com.taxidata.domain.TaxiTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaxiTripRepository extends JpaRepository<TaxiTrip, Long> {
    
    @Query("SELECT t FROM TaxiTrip t WHERE t.pickupDatetime >= :startDate AND t.pickupDatetime <= :endDate")
    List<TaxiTrip> findTripsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT t FROM TaxiTrip t 
        WHERE t.pickupDatetime >= :startDate 
        AND t.pickupDatetime <= :endDate 
        ORDER BY t.fareAmount DESC
        """)
    List<TaxiTrip> findTopFareTrips(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COUNT(t) 
        FROM TaxiTrip t 
        WHERE t.pickupDatetime >= :startDate 
        AND t.pickupDatetime <= :endDate
        """)
    long countTripsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT AVG(t.fareAmount) 
        FROM TaxiTrip t 
        WHERE t.pickupDatetime >= :startDate 
        AND t.pickupDatetime <= :endDate
        """)
    Double calculateAverageFare(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Method to find failed records that need retry
    @Query(value = """
        SELECT t FROM TaxiTrip t 
        WHERE t.version = 0 
        AND t.createdAt < :cutoffTime
        """)
    List<TaxiTrip> findFailedRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
}