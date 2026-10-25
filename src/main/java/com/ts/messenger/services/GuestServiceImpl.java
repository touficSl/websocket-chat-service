package com.ts.messenger.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.repositories.GuestEntityRepository;
import com.ts.messenger.utils.SecureTokens;

@Service("guestService")
public class GuestServiceImpl implements GuestService {

	private static final Logger LOG = LoggerFactory.getLogger(GuestServiceImpl.class);

	/* 256 bits of entropy */
	private static final int CHAT_TOKEN_BYTES = 32;

	@Autowired
	private GuestEntityRepository guestEntityRepository;

	@Override
	public GuestRoomEntity createGuest(String username, String email, String roomNumber, String roomTypeCode, String hotelId) {

		try {

			GuestRoomEntity guest = new GuestRoomEntity();
			guest.setGuestUsername(username);
			guest.setEmail(email);
			guest.setRoomNumber(roomNumber);
			guest.setRoomTypeCode(roomTypeCode);
			guest.sethotelId(hotelId);
			guest.setDate(new Date());
			guest.setSecurityToken(SecureTokens.generate(CHAT_TOKEN_BYTES));
			return guestEntityRepository.save(guest);

		} catch (Exception e) {
			LOG.error("Unable to create guest room", e);
			return null;
		}
	}

	@Override
	public GuestRoomEntity checkIfExist(String roomNumber, String nerochaHotelId) {
		return guestEntityRepository.findGuestRoom(roomNumber, nerochaHotelId).orElse(null);
	}

	@Override
	public GuestRoomEntity findAuthorizedChat(String roomNumber, String hotelId, String guestUsername, String chatSecurityToken) {
		GuestRoomEntity guestRoom = checkIfExist(roomNumber, hotelId);
		if (guestRoom == null || guestRoom.getGuestUsername() == null || !guestRoom.getGuestUsername().equals(guestUsername))
			return null;
		if (!SecureTokens.equals(guestRoom.getSecurityToken(), chatSecurityToken))
			return null;
		return guestRoom;
	}

	@Override
	public GuestRoomEntity addHotel(GuestRoomEntity guest, String username) {
		try {
			guest.setHotelUsername(username);
			return guestEntityRepository.save(guest);
		} catch (Exception e) {
			LOG.error("Unable to assign hotel user to guest room", e);
			return null;
		}
	}

	@Override
	public GuestRoomEntity save(GuestRoomEntity guestRoomEntity) {
		try {
			return guestEntityRepository.save(guestRoomEntity);
		} catch (Exception e) {
			LOG.error("Unable to save guest room", e);
			return null;
		}
	}

	@Override
	public List<GuestRoomEntity> getGuestRooms(String hotelUsername, String hotelId) {
		return guestEntityRepository.getGuestRooms(hotelUsername, hotelId);
	}

	@Override
	public int logOut(String hotelId, String roomNumber, String guestUsername, String chatSecurityToken, String loggedOutDate) {
		return guestEntityRepository.logOut(hotelId, roomNumber, guestUsername, chatSecurityToken, loggedOutDate);
	}

	@Override
	public GuestRoomEntity checkIfExistGestUsername(String guestUsername) {
		return guestEntityRepository.checkIfExistGestUsername(guestUsername);
	}
}
