CREATE TABLE failed_records (
    id BIGSERIAL PRIMARY KEY,
    record_id VARCHAR(255) NOT NULL,
    record_data TEXT NOT NULL,
    error_message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT uk_failed_records_record_id UNIQUE (record_id)
);

CREATE INDEX idx_failed_records_created_at ON failed_records(created_at); 