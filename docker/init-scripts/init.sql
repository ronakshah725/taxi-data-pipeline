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

CREATE INDEX idx_taxi_trips_pickup_datetime ON taxi_trips(pickup_datetime);
CREATE INDEX idx_taxi_trips_dropoff_datetime ON taxi_trips(dropoff_datetime);

CREATE TABLE failed_records (
    id BIGSERIAL PRIMARY KEY,
    record_id VARCHAR(255) NOT NULL,
    record_data TEXT NOT NULL,
    error_message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_failed_records_record_id UNIQUE (record_id)
);

CREATE INDEX idx_failed_records_created_at ON failed_records(created_at);

-- Table for job checkpoints
CREATE TABLE job_checkpoints (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    checkpoint_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);