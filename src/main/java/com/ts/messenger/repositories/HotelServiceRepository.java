package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.ts.messenger.entities.HotelServices;

@RepositoryRestResource
public interface HotelServiceRepository extends JpaRepository<HotelServices, Integer> {

	 @Query(value = "SELECT * FROM hotel_service as hs, service as s where hs.hotel_id = :hotelId and s.service_name= :serviceName and s.id = hs.service_id and hs.enabled = true", nativeQuery = true)
	 HotelServices findHotelServiceByHotelId(@Param("hotelId") String hotelId, @Param("serviceName") String serviceName); 
}