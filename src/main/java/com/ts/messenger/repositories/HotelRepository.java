package com.ts.messenger.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.ts.messenger.entities.Hotel;

@RepositoryRestResource
public interface HotelRepository extends JpaRepository<Hotel, String>, JpaSpecificationExecutor<Hotel> {

    @Query(value = "SELECT * FROM hotel as h where h.hotel_id = :hotelId", nativeQuery = true)
    Hotel findByhotelId(@Param("hotelId") String hotelId);

	List<Hotel> findByKioskHotelCodeIsNotNull();
}