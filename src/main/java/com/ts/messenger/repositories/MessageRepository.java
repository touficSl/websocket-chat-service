package com.ts.messenger.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.MessageEntity;

@Repository
@Transactional
public interface MessageRepository extends JpaRepository<MessageEntity, Integer> {

	@Query(value = "FROM MessageEntity WHERE guestRoom.hotelId = :hotelId AND roomNumber = :roomNumber AND guestRoom.guestUsername = :guestUserName ORDER BY createdDate DESC", nativeQuery = false)
	List<MessageEntity> chatMessages(@Param("guestUserName") String guestUserName, @Param("hotelId") String hotelId, @Param("roomNumber") String roomNumber, Pageable pageable);

	@Query(value = "SELECT COUNT(m) FROM MessageEntity m WHERE m.guestRoom.hotelId = :hotelId AND m.roomNumber = :roomNumber AND m.guestRoom.guestUsername = :guestUserName", nativeQuery = false)
	long countChatMessages(@Param("guestUserName") String guestUserName, @Param("hotelId") String hotelId, @Param("roomNumber") String roomNumber);

    @Modifying
    @Query(value = "UPDATE messenger_message SET status = :status WHERE room_number = :roomNumber AND receiver = :receiver AND guest_room_id = :guestRoomId ", nativeQuery = true)
    int updateMessageStatus(@Param("roomNumber") String roomNumber, @Param("receiver") String receiver, @Param("guestRoomId") int guestRoomId, @Param("status") String status);
}
