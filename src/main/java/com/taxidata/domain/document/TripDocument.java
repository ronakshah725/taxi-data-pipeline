package com.taxidata.domain.document;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Document(indexName = "taxi-trips")
public class TripDocument {

    private static final String DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:ss";
    @Id
    private String id;

    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_FORMAT)
    private LocalDateTime pickupDatetime;

    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_FORMAT)
    private LocalDateTime dropoffDatetime;

    @Field(type = FieldType.Integer)
    private Integer passengerCount;

    @Field(type = FieldType.Double)
    private BigDecimal tripDistance;

    @Field(type = FieldType.Double)
    private BigDecimal fareAmount;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Version
    private Long version;

    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdDate;

    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_FORMAT)
    private LocalDateTime lastModifiedDate;
}
