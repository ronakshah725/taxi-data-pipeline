package com.taxidata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@ConfigurationProperties(prefix = "batch")
@Validated
@Data
public class BatchProperties {
    
    @NotNull
    private JobProperties job = new JobProperties();
    
    @Data
    public static class JobProperties {
        @NotEmpty
        private String name = "taxiDataImportJob";
        
        @NotNull
        private StepProperties step = new StepProperties();
        
        @Min(1)
        private int chunkSize = 1000;
        
        @NotNull
        private RetryProperties retry = new RetryProperties();
        
        @NotNull
        private SkipProperties skip = new SkipProperties();
    }
    
    @Data
    public static class StepProperties {
        @NotEmpty
        private String name = "importTaxiDataStep";
    }
    
    @Data
    public static class RetryProperties {
        @Min(1)
        private int limit = 3;
    }
    
    @Data
    public static class SkipProperties {
        @Min(1)
        private int limit = 10;
    }
} 