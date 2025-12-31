package com.ts.messenger.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import com.ts.messenger.entities.GuestRoomEntity;

@RepositoryRestResource
@Transactional
public interface GuestEntityRepository extends JpaRepository<GuestRoomEntity, String>, JpaSpecificationExecutor<GuestRoomEntity> {

    @Query(value = "SELECT * FROM messenger_guest_room AS u WHERE u.room_number = :roomNumber AND u.hotel_id = :hotelId AND logged_out IS NULL ", nativeQuery = true)
    Optional<GuestRoomEntity> findGuestRoom(@Param("roomNumber") String roomId, @Param("hotelId") String hotelId);

    /* ORDER BY logged_out ASC, return the NULL logged_out first */
    @Query(value = "SELECT * FROM messenger_guest_room AS u WHERE (u.hotel_username = :hotelUsername OR u.hotel_username IS NULL) AND u.hotel_id = :hotelId AND logged_out IS NULL ", nativeQuery = true)
    List<GuestRoomEntity> getGuestRooms(@Param("hotelUsername") String hotelUsername, @Param("hotelId") String hotelId);

    @Modifying
    @Query(value = "UPDATE messenger_guest_room SET logged_out = :loggedOutDate WHERE room_number = :roomNumber AND hotel_id = :hotelId AND guest_username = :guestUsername AND security_token = :chatSecurityToken ", nativeQuery = true)
    int logOut(@Param("hotelId") String hotelId, @Param("roomNumber") String roomNumber, @Param("guestUsername") String guestUsername, @Param("chatSecurityToken") String chatSecurityToken, @Param("loggedOutDate") String loggedOutDate);

    @Query(value = "SELECT * FROM messenger_guest_room WHERE guest_username = :guestUsername AND logged_out IS NULL ", nativeQuery = true)
	GuestRoomEntity checkIfExistGestUsername(@Param("guestUsername") String guestUsername);
    
}