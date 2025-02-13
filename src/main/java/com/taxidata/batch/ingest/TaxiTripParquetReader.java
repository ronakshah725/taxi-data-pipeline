package com.taxidata.batch.ingest;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiTripParquetReader implements ItemReader<TaxiTrip> {

    @Valid
    private final ParquetReaderConfig config;
    
    private ParquetReader<Group> reader;
    private long recordCount = 0;

    @PostConstruct
    public void initialize() throws IOException {
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
            .vendorId(group.getInteger("VendorID", 0))
            .paymentType(getLongAsInt(group, "payment_type"))
            .passengerCount(getLongAsInt(group, "passenger_count"))
            .tripDistance(getBigDecimalValue(group, "trip_distance"))
            .fareAmount(getBigDecimalValue(group, "fare_amount"))
            .totalAmount(getBigDecimalValue(group, "total_amount"))
            .build();
    }

    private Integer getLongAsInt(Group group, String fieldName) {
        try {
            return (int) group.getLong(fieldName, 0);
        } catch (Exception e) {
            log.warn("Invalid integer value for field {}", fieldName);
            return 0;
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