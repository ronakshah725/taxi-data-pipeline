package com.taxidata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class IndexingScheduleConfig {
    private final JobLauncher jobLauncher;
    private final Job elasticsearchIndexingJob;
    private final JobExplorer jobExplorer;

    private static final int DELAY_SECONDS = 360;
    private static final int LAST_SYNC_SECONDS = 2 * DELAY_SECONDS;

    private LocalDateTime lastSuccessfulSync = LocalDateTime.now().minusSeconds(LAST_SYNC_SECONDS);

    @Scheduled(fixedDelay = DELAY_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void triggerIndexing() {
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

    // Check if Job there is stuck in pending for longer than lastSuccessfulSync
    private boolean recentJobsStillRunning() {
        return jobExplorer.findRunningJobExecutions("elasticsearchIndexingJob")
                .stream().filter(jobExecution ->
                        Duration.between(
                                LocalDateTime.now(), jobExecution.getCreateTime()).toSeconds() > LAST_SYNC_SECONDS)
                .count() > 0;
    }
}
