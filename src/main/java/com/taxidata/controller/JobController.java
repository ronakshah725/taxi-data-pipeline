package com.taxidata.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import java.time.LocalDateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.batch.core.explore.JobExplorer;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    // private final JobExplorer jobExplorer;
    private final Job taxiDataImportJob;

    @PostMapping("/taxi-import")
    public ResponseEntity<String> startTaxiImport() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
                
            log.info("Starting taxi import job");
            JobExecution jobExecution = jobLauncher.run(taxiDataImportJob, params);

            return ResponseEntity.ok("Job id:" + jobExecution.getJobId());

        } catch (Exception e) {
            log.error("Failed to start job", e);
            return ResponseEntity.internalServerError().body("Failed to start job: " + e.getMessage());
        }
    }

    // @GetMapping("/taxi-import/{jobId}/status")
    // public ResponseEntity<StatusDTO> getProgress(@PathVariable Long jobId) {
    //     JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
        
    //     if (jobExecution == null) {
    //         return ResponseEntity.notFound().build();
    //     }

    //     // Get job status
    //     BatchStatus jobStatus = jobExecution.getStatus();

    //     // Calculate progress
    //     long totalWriteCount = jobExecution.getStepExecutions().stream()
    //         .mapToLong(StepExecution::getWriteCount)
    //         .sum();

    //     // Create and return status DTO
    //     StatusDTO statusDTO = new StatusDTO(jobStatus, totalWriteCount, LocalDateTime.now());
    //     return ResponseEntity.ok(statusDTO);
    // }
} 