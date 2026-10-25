package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, String> {

    @Query(value = "SELECT * FROM hotel as h where h.hotel_id = :hotelId", nativeQuery = true)
    Hotel findByhotelId(@Param("hotelId") String hotelId);
}
