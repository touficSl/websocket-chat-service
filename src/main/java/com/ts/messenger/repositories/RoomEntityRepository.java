package com.ts.messenger.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.ts.messenger.entities.RoomEntity;

@RepositoryRestResource
public interface RoomEntityRepository extends JpaRepository<RoomEntity, String>, JpaSpecificationExecutor<RoomEntity> {

	RoomEntity findByRoomCode(String roomCode);
}