package com.taxidata.batch.ingest;

import com.taxidata.batch.TestDataGenerator;
import com.taxidata.config.ParquetReaderConfig;
import com.taxidata.domain.TaxiTrip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TaxiTripParquetReaderTest {

    @TempDir
    File tempDir;

    private TaxiTripParquetReader reader;
    private ParquetReaderConfig config;
    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = new File(tempDir, "test_taxi_data.parquet");
        TestDataGenerator.generateSampleParquetFile(testFile.getAbsolutePath(), 10);

        config = Mockito.mock(ParquetReaderConfig.class);
        when(config.getPath()).thenReturn(testFile.getAbsolutePath());
        when(config.getBatchSize()).thenReturn(100);

        reader = new TaxiTripParquetReader(config);
        reader.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        reader.cleanup();
        Files.deleteIfExists(testFile.toPath());
    }

    @Test
    void shouldReadParquetFileSuccessfully() throws Exception {
        List<TaxiTrip> trips = new ArrayList<>();
        TaxiTrip trip;
        while ((trip = reader.read()) != null) {
            trips.add(trip);
        }

        assertThat(trips).hasSize(10)
                .allSatisfy(t -> {
                    assertThat(t.getVendorId()).isIn(1, 2);
                    assertThat(t.getPaymentType()).isBetween(1, 4);
                    assertThat(t.getPassengerCount()).isBetween(1, 3);
                    assertThat(t.getTripDistance()).isPositive();
                    assertThat(t.getFareAmount()).isPositive();
                    assertThat(t.getTotalAmount()).isGreaterThan(t.getFareAmount());
                });

        TaxiTrip firstTrip = trips.get(0);
        assertThat(firstTrip)
                .matches(t -> t.getVendorId() == 1)
                .matches(t -> t.getPaymentType() == 1)
                .matches(t -> t.getPassengerCount() == 1)
                .matches(t -> t.getTripDistance().compareTo(BigDecimal.valueOf(2.5)) == 0)
                .matches(t -> t.getFareAmount().compareTo(BigDecimal.valueOf(20.0)) == 0)
                .matches(t -> t.getTotalAmount().compareTo(BigDecimal.valueOf(25.0)) == 0);
    }
}