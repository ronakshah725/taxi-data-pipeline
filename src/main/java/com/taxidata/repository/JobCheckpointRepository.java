package com.taxidata.repository;

import com.taxidata.domain.JobCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobCheckpointRepository extends JpaRepository<JobCheckpoint, Long> {
    Optional<JobCheckpoint> findTopByJobNameOrderByCreatedAtDesc(String jobName);
}