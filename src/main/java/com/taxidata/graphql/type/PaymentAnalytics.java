package com.taxidata.graphql.type;

import java.util.List;

import lombok.Value;

@Value
public class PaymentAnalytics {
    List<PaymentTypeCount> paymentTypeCounts;
    List<PaymentTypeFare> averageFareByPaymentType;
}