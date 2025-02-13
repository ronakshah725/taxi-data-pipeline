package com.taxidata.batch.search;

import com.taxidata.domain.TaxiTrip;
import com.taxidata.domain.document.TripDocument;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TripDocumentProcessor implements ItemProcessor<TaxiTrip, TripDocument> {
    
    @Override
    public TripDocument process(TaxiTrip trip) {
        // TODO: Handle null trip
        if (trip == null) {
            return null;
        }

        TripDocument document = new TripDocument();
        
        document.setId(trip.getId() != null ? trip.getId().toString() : null);
        
        document.setPickupDatetime(trip.getPickupDatetime());
        document.setDropoffDatetime(trip.getDropoffDatetime());
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
