package com.taxidata.batch;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.repository.TaxiTripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(properties = {
        "spring.elasticsearch.enabled=false"
})
@SpringBatchTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestConfig.class)
@Disabled
class BatchIngestionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("taxidb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofSeconds(30));

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job taxiDataImportJob;

    @TempDir
    static Path tempDir;

    @Autowired
    private TaxiTripRepository taxiTripRepository;

    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("test_taxi_data.parquet").toFile();
        TestDataGenerator.generateSampleParquetFile(testFile.getAbsolutePath(), 10);
    }

    @Test
    void shouldProcessParquetFileAndPersistToDatabase() throws Exception {
        taxiTripRepository.deleteAll();

        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(taxiDataImportJob, params);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getJobInstance().getJobName()).isEqualTo("taxiDataImportJob");

        List<TaxiTrip> savedTrips = taxiTripRepository.findAll();
        assertThat(savedTrips).hasSize(10);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getReadCount()).isEqualTo(10);
        assertThat(stepExecution.getWriteCount()).isEqualTo(10);
        assertThat(stepExecution.getSkipCount()).isZero();
    }
}