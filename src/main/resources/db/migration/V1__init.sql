CREATE TABLE taxi_trips (
    id BIGSERIAL PRIMARY KEY,
    vendor_id INTEGER,
    payment_type INTEGER,
    passenger_count INTEGER NOT NULL,
    trip_distance DECIMAL(10,2) NOT NULL,
    fare_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for common queries
CREATE INDEX idx_taxi_trips_vendor_id ON taxi_trips(vendor_id);
CREATE INDEX idx_taxi_trips_payment_type ON taxi_trips(payment_type);
CREATE INDEX idx_taxi_trips_fare_amount ON taxi_trips(fare_amount);
CREATE INDEX idx_taxi_trips_total_amount ON taxi_trips(total_amount); 