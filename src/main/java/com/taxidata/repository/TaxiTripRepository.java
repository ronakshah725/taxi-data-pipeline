package com.taxidata.repository;

import com.taxidata.domain.TaxiTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiTripRepository extends JpaRepository<TaxiTrip, Long> {}