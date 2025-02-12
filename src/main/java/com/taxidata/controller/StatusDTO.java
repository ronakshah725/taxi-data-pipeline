package com.taxidata.controller;

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class StatusDTO {
    private final BatchStatus status;
    private final long progress;
    private final LocalDateTime timestamp;
}
