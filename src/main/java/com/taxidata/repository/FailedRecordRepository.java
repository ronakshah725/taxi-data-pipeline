package com.taxidata.repository;

import com.taxidata.domain.FailedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedRecordRepository extends JpaRepository<FailedRecord, Long> {
}