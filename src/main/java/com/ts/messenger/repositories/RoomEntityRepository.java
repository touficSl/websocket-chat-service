package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.RoomEntity;

@Repository
public interface RoomEntityRepository extends JpaRepository<RoomEntity, Integer> {
	RoomEntity findByRoomCode(String roomCode);
}
