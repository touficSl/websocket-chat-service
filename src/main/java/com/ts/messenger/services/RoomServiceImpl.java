package com.ts.messenger.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.RoomEntity;
import com.ts.messenger.repositories.RoomEntityRepository;

@Service("roomService")
public class RoomServiceImpl implements RoomService {

	@Autowired
	private RoomEntityRepository roomEntityRepository;

	@Override
	public RoomEntity findByRoomCode(String roomCode) {
		return roomEntityRepository.findByRoomCode(roomCode);
	}

	
}
