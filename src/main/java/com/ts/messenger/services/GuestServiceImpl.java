package com.ts.messenger.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.repositories.GuestEntityRepository;
import com.ts.messenger.utils.PasswordGenerator;

@Service("guestService")
public class GuestServiceImpl implements GuestService {
	
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
			guest.setSecurityToken(generateChatSecurityToken(username, roomNumber, roomTypeCode));
			
			return guestEntityRepository.save(guest);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String generateChatSecurityToken(String username, String roomNumber, String roomTypeCode) {
		String chatSecToken = "#" + username + "." + roomTypeCode + "." + roomNumber + "." +PasswordGenerator.generatePassword() + "#";
		return chatSecToken;
	}

	@Override
	public GuestRoomEntity checkIfExist(String roomNumber, String nerochaHotelId) {
		
		GuestRoomEntity user = null;
		Optional<GuestRoomEntity> optionalUser = guestEntityRepository.findGuestRoom(roomNumber, nerochaHotelId);
		if(optionalUser.isPresent()) user = optionalUser.get();
		return user;
		
	}

	@Override
	public GuestRoomEntity addHotel(GuestRoomEntity guest, String username) {
		try {
			guest.setHotelUsername(username);
			return guestEntityRepository.save(guest);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public GuestRoomEntity save(GuestRoomEntity guestRoomEntity) {
		try {
			return guestEntityRepository.save(guestRoomEntity);
		} catch (Exception e) {
			e.printStackTrace();
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
