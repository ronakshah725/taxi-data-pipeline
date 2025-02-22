package com.taxidata.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taxi_trips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TaxiTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = true)
    private Integer vendorId;
    
    @Column(name = "payment_type", nullable = true)
    private Integer paymentType;
    
    @NotNull(message = "Passenger count is required")
    // @Min(value = 1, message = "Passenger count must be at least 1")
    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;
    
    @NotNull(message = "Trip distance is required")
    // @DecimalMin(value = "0.0", message = "Trip distance must be positive")
    @Column(name = "trip_distance", nullable = false, precision = 10, scale = 2)
    private BigDecimal tripDistance;
    
    @NotNull(message = "Fare amount is required")
    // @DecimalMin(value = "0.0", message = "Fare amount must be positive")
    @Column(name = "fare_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal fareAmount;
    
    @NotNull(message = "Total amount is required")
    // @DecimalMin(value = "0.0", message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Version
    private Long version;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}