package com.ts.messenger.services;

import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;
 
public interface HotelService {

	Hotel getOne(String code);

	HotelServices findHotelServiceByHotelId(String hotelId);
	
}
