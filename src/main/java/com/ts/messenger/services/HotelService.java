package com.ts.messenger.services;

import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;

public interface HotelService {

	Hotel getOne(String code);

	HotelServices findHotelServiceByHotelId(String hotelId);

	/** IANA timezone of the hotel, "-1" when the hotel is unknown, null when not configured */
	String getHotelTimezone(String hotelId);

}
