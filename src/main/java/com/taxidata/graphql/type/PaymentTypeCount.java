package com.taxidata.graphql.type;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class PaymentTypeCount {
    int paymentType;
    int count;
    BigDecimal totalRevenue;
}