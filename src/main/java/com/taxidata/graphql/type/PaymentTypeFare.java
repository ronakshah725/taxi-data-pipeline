package com.taxidata.graphql.type;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class PaymentTypeFare {
    int paymentType;
    BigDecimal averageFare;
}