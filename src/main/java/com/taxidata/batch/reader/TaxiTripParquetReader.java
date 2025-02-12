package com.taxidata.batch.reader;

import com.taxidata.config.ParquetReaderConfig;
import com.taxidata.domain.TaxiTrip;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.example.data.Group;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiTripParquetReader implements ItemReader<TaxiTrip> {

    @Valid
    private final ParquetReaderConfig config;
    
    private ParquetReader<Group> reader;
    private long recordCount = 0;
    private DateTimeFormatter dateTimeFormatter;

    @PostConstruct
    public void initialize() throws IOException {
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(config.getDateTimeFormat());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid date time format: " + config.getDateTimeFormat(), e);
        }
        
        Path path = new Path(config.getPath());
        try {
            reader = ParquetReader.builder(new GroupReadSupport(), path)
                .withConf(new Configuration())
                .build();
            log.info("Initialized ParquetReader for file: {}", config.getPath());
        } catch (IOException e) {
            log.error("Failed to initialize ParquetReader for file: {}", config.getPath(), e);
            throw new IllegalStateException("Could not initialize Parquet reader", e);
        }
    }

    @Override
    public TaxiTrip read() throws Exception {
        try {
            Group group = reader.read();
            if (group == null) {
                log.info("Completed reading {} records", recordCount);
                return null;
            }

            recordCount++;
            if (recordCount % config.getBatchSize() == 0) {
                log.info("Processing record #{}", recordCount);
            }

            return convertToTaxiTrip(group);
        } catch (Exception e) {
            log.error("Error reading record #{}", recordCount, e);
            throw new IllegalStateException("Failed to read record", e);
        }
    }

    private TaxiTrip convertToTaxiTrip(Group group) {
        return TaxiTrip.builder()
            .pickupDatetime(getDateTimeValue(group, "pickup_datetime"))
            .dropoffDatetime(getDateTimeValue(group, "dropoff_datetime"))
            .passengerCount(getIntegerValue(group, "passenger_count"))
            .tripDistance(getBigDecimalValue(group, "trip_distance"))
            .fareAmount(getBigDecimalValue(group, "fare_amount"))
            .totalAmount(getBigDecimalValue(group, "total_amount"))
            .build();
    }

    private Integer getIntegerValue(Group group, String fieldName) {
        try {
            return group.getInteger(fieldName, 0);
        } catch (Exception e) {
            log.warn("Invalid integer value for field {}", fieldName);
            return 0;
        }
    }

    private LocalDateTime getDateTimeValue(Group group, String fieldName) {
        try {
            String dateTimeStr = group.getString(fieldName, 0);
            return LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            log.error("Invalid datetime format for field {}: {}", fieldName, e.getMessage());
            throw e;
        }
    }

    private BigDecimal getBigDecimalValue(Group group, String fieldName) {
        try {
            return BigDecimal.valueOf(group.getDouble(fieldName, 0));
        } catch (Exception e) {
            log.warn("Invalid decimal value for field {}", fieldName);
            return BigDecimal.ZERO;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (reader != null) {
            try {
                reader.close();
                log.info("Closed ParquetReader after processing {} records", recordCount);
            } catch (IOException e) {
                log.error("Error closing ParquetReader", e);
            }
        }
    }
}