package com.taxidata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxidata.domain.JobCheckpoint;
import com.taxidata.repository.JobCheckpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchJobService {
    
    private final JobLauncher jobLauncher;
    private final Job taxiDataImportJob;
    private final JobCheckpointRepository checkpointRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public JobExecution startJob(String filePath) throws Exception {
        try {
            JobParameters parameters = createJobParameters(filePath);
            log.info("Starting taxi data import job with parameters: {}", parameters);
            return jobLauncher.run(taxiDataImportJob, parameters);
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job is already running", e);
            throw e;
        } catch (JobRestartException e) {
            log.error("Job cannot be restarted", e);
            throw e;
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Job instance already completed", e);
            throw e;
        } catch (Exception e) {
            log.error("Error running job", e);
            throw e;
        }
    }

    @Transactional
    public void saveCheckpoint(String jobName, Map<String, Object> checkpointData) {
        try {
            String checkpointJson = objectMapper.writeValueAsString(checkpointData);
            JobCheckpoint checkpoint = JobCheckpoint.builder()
                .jobName(jobName)
                .checkpointData(checkpointJson)
                .build();
            checkpointRepository.save(checkpoint);
            log.info("Saved checkpoint for job: {}", jobName);
        } catch (Exception e) {
            log.error("Error saving checkpoint", e);
            throw new RuntimeException("Failed to save checkpoint", e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> loadCheckpoint(String jobName) {
        try {
            return checkpointRepository.findTopByJobNameOrderByCreatedAtDesc(jobName)
                .map(checkpoint -> {
                    try {
                        return objectMapper.readValue(checkpoint.getCheckpointData(), 
                            new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, Object>>() {});
                    } catch (Exception e) {
                        log.error("Error deserializing checkpoint data", e);
                        throw new RuntimeException("Failed to deserialize checkpoint data", e);
                    }
                })
                .orElse(new HashMap<>());
        } catch (Exception e) {
            log.error("Error loading checkpoint", e);
            throw new RuntimeException("Failed to load checkpoint", e);
        }
    }

    private JobParameters createJobParameters(String filePath) {
        return new JobParametersBuilder()
            .addString("filePath", filePath)
            .addString("timestamp", LocalDateTime.now().toString())
            .toJobParameters();
    }
}