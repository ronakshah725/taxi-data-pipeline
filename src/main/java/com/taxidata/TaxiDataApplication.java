package com.taxidata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.taxidata.config.ParquetReaderConfig;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(ParquetReaderConfig.class)
public class TaxiDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaxiDataApplication.class, args);
    }
}