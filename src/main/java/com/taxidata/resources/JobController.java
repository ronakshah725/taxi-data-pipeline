package com.taxidata.resources;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
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

} 