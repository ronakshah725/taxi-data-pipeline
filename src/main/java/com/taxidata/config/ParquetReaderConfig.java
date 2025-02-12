package com.taxidata.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "input.file")
public class ParquetReaderConfig {
    
    @NotBlank(message = "Input file path must be configured")
    private String path;
    
    @Min(value = 100, message = "Batch size must be at least 100")
    private int batchSize = 1000;
    
    private String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss";
} 