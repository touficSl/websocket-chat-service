package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.HotelServices;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelServices, Integer> {

	 @Query(value = "SELECT hs.* FROM hotel_service as hs, service as s where hs.hotel_id = :hotelId and s.service_name= :serviceName and s.id = hs.service_id and hs.enabled = true LIMIT 1", nativeQuery = true)
	 HotelServices findHotelServiceByHotelId(@Param("hotelId") String hotelId, @Param("serviceName") String serviceName);
}
