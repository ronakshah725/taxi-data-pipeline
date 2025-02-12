package com.taxidata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobMetricsService {
    
    public Map<String, Object> collectJobMetrics(JobExecution jobExecution) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("jobId", jobExecution.getJobId());
        metrics.put("status", jobExecution.getStatus());
        metrics.put("startTime", jobExecution.getStartTime());
        metrics.put("endTime", jobExecution.getEndTime());
        metrics.put("duration", calculateDuration(jobExecution));
        
        Map<String, Object> stepMetrics = new HashMap<>();
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            stepMetrics.put(stepExecution.getStepName(), collectStepMetrics(stepExecution));
        }
        metrics.put("steps", stepMetrics);
        
        log.info("Job metrics: {}", metrics);
        return metrics;
    }

    private Map<String, Object> collectStepMetrics(StepExecution stepExecution) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("readCount", stepExecution.getReadCount());
        metrics.put("writeCount", stepExecution.getWriteCount());
        metrics.put("skipCount", stepExecution.getSkipCount());
        
        if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
            Duration duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
            metrics.put("duration", duration.toSeconds());
            
            long totalProcessed = stepExecution.getWriteCount();
            double ratePerSecond = duration.toSeconds() > 0 
                ? totalProcessed / (double) duration.toSeconds() 
                : 0.0;
            metrics.put("recordsPerSecond", String.format("%.2f", ratePerSecond));
        }
        
        return metrics;
    }

    private long calculateDuration(JobExecution jobExecution) {
        if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
            return Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toSeconds();
        }
        return 0;
    }
}