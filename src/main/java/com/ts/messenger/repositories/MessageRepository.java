package com.ts.messenger.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import com.ts.messenger.entities.MessageEntity;

@RepositoryRestResource
@Transactional
public interface MessageRepository extends JpaRepository<MessageEntity, String>, JpaSpecificationExecutor<MessageEntity> {

	@Query(value = "FROM MessageEntity WHERE guestRoom.hotelId = :hotelId AND roomNumber = :roomNumber AND guestRoom.guestUsername = :guestUserName ORDER BY createdDate DESC", nativeQuery = false)
	List<MessageEntity> chatMessages(@Param("guestUserName") String guestUserName, @Param("hotelId") String hotelId, @Param("roomNumber") String roomNumber, Pageable pageable);

	@Query(value = "FROM MessageEntity WHERE guestRoom.hotelId = :hotelId AND roomNumber = :roomNumber AND guestRoom.guestUsername = :guestUserName ORDER BY createdDate DESC", nativeQuery = false)
	List<MessageEntity> allChatMessages(@Param("guestUserName") String guestUserName, @Param("hotelId") String hotelId, @Param("roomNumber") String roomNumber);

    @Modifying
    @Query(value = "UPDATE messenger_message SET status = :status WHERE room_number = :roomNumber AND receiver = :receiver AND guest_room_id = :guestRoomId ", nativeQuery = true)
    int updateMessageStatus(@Param("roomNumber") String roomNumber, @Param("receiver") String receiver, @Param("guestRoomId") int guestRoomId, @Param("status") String status);

}