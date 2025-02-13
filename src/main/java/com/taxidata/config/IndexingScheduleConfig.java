package com.taxidata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class IndexingScheduleConfig {
    private final JobLauncher jobLauncher;
    private final Job elasticsearchIndexingJob;
    private final JobExplorer jobExplorer;
    
    private LocalDateTime lastSuccessfulSync = LocalDateTime.now().minusMinutes(10);

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void triggerIndexing() {
        if (!jobExplorer.findRunningJobExecutions("elasticsearchIndexingJob").isEmpty()) {
            log.info("Previous indexing job is still running. Skipping this execution.");
            return;
        }

        try {
            JobParameters params = new JobParametersBuilder()
                .addString("lastSync", lastSuccessfulSync.toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            
            log.info("Starting indexing job with lastSync: {}", lastSuccessfulSync);
            JobExecution execution = jobLauncher.run(elasticsearchIndexingJob, params);
            
            if (execution.getStatus() == BatchStatus.COMPLETED) {
                lastSuccessfulSync = LocalDateTime.now();
                log.info("Indexing job completed successfully. Updated lastSync to: {}", lastSuccessfulSync);
            } else {
                log.warn("Indexing job completed with status: {}", execution.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to start indexing job", e);
        }
    }
}
