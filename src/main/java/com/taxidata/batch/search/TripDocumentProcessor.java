package com.taxidata.batch.search;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.domain.document.TripDocument;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TripDocumentProcessor implements ItemProcessor<TaxiTrip, TripDocument> {
    
    @Override
    public TripDocument process(TaxiTrip trip) {
        TripDocument document = new TripDocument();
        
        document.setId(trip.getId() != null ? trip.getId().toString() : null);
        
        document.setVendorId(trip.getVendorId());
        document.setPaymentType(trip.getPaymentType());
        document.setPassengerCount(trip.getPassengerCount());
        document.setTripDistance(trip.getTripDistance());
        document.setFareAmount(trip.getFareAmount());
        document.setTotalAmount(trip.getTotalAmount());
        document.setVersion(trip.getVersion());
        document.setCreatedDate(trip.getCreatedAt());
        document.setLastModifiedDate(trip.getUpdatedAt());
        
        return document;
    }
}
