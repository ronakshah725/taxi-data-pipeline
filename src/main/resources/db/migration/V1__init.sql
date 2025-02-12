CREATE TABLE taxi_trips (
    id BIGSERIAL PRIMARY KEY,
    pickup_datetime TIMESTAMP NOT NULL,
    dropoff_datetime TIMESTAMP NOT NULL,
    passenger_count INTEGER NOT NULL,
    trip_distance DECIMAL(10,2) NOT NULL,
    fare_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for common queries
CREATE INDEX idx_taxi_trips_pickup_datetime ON taxi_trips(pickup_datetime);
CREATE INDEX idx_taxi_trips_dropoff_datetime ON taxi_trips(dropoff_datetime);
CREATE INDEX idx_taxi_trips_fare_amount ON taxi_trips(fare_amount);
CREATE INDEX idx_taxi_trips_total_amount ON taxi_trips(total_amount); 