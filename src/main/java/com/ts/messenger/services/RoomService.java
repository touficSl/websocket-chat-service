package com.ts.messenger.services;

import com.ts.messenger.entities.RoomEntity;

public interface RoomService {

	RoomEntity findByRoomCode(String roomCode);

}
