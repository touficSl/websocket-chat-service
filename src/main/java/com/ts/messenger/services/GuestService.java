package com.ts.messenger.services;

import java.util.List;

import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.utils.Response;

public interface GuestService {

	public GuestRoomEntity createGuest(String username, String email, String roomNumber, String roomTypeCode, String hotelId);
	
	public GuestRoomEntity addHotel(GuestRoomEntity guestRoomEntity, String username);

	/* roomId + hotelId = unique */
	public GuestRoomEntity checkIfExist(String roomId, String nerochaHotelId);

	public GuestRoomEntity save(GuestRoomEntity guestRoomEntity);

	public List<GuestRoomEntity> getGuestRooms(String hotelUsername, String hotelId);

	public int logOut(String hotelId, String roomNumber, String guestUsername, String chatSecurityToken, String loggedOutDate);

	public GuestRoomEntity checkIfExistGestUsername(String guestUsername);
	
}
