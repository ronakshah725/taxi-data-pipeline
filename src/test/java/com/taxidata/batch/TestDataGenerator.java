package com.taxidata.batch;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;

import java.io.File;

public class TestDataGenerator {

    private static final String SCHEMA = """
        message taxi_trip {
          required int32 VendorID;
          required int64 payment_type;
          required int64 passenger_count;
          required double trip_distance;
          required double fare_amount;
          required double total_amount;
        }
        """;

    public static void generateSampleParquetFile(String outputPath, int count) throws Exception {
        Configuration conf = new Configuration();
        MessageType schema = MessageTypeParser.parseMessageType(SCHEMA);
        GroupWriteSupport writeSupport = new GroupWriteSupport();
        GroupWriteSupport.setSchema(schema, conf);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();

        try (ParquetWriter<Group> writer = new ParquetWriter<>(
                new Path(outputFile.getAbsolutePath()),
                writeSupport,
                ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
                ParquetWriter.DEFAULT_BLOCK_SIZE,
                ParquetWriter.DEFAULT_PAGE_SIZE,
                ParquetWriter.DEFAULT_PAGE_SIZE,
                ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
                ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
                ParquetWriter.DEFAULT_WRITER_VERSION,
                conf)) {

            // Generate 10 sample records
            for (int i = 0; i < count; i++) {
                Group record = new SimpleGroup(schema);
                record.add("VendorID", (i % 2) + 1);  // Alternates between 1 and 2
                record.add("payment_type", (long)((i % 4) + 1));  // Values 1-4
                record.add("passenger_count", (long)(1 + (i % 3)));  // Values 1-3
                record.add("trip_distance", 2.5 + i);  // Increasing distances
                record.add("fare_amount", 20.0 + (i * 5));  // Increasing fares
                record.add("total_amount", 25.0 + (i * 5));  // Total > fare
                writer.write(record);
            }
        }
    }
}