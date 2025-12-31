package com.ts.messenger.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;
import com.ts.messenger.repositories.HotelRepository;
import com.ts.messenger.repositories.HotelServiceRepository;
import com.ts.messenger.utils.Constants;

@Service("hotelService")
@Repository 
public class HotelServiceImpl implements HotelService {
	
	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private HotelServiceRepository hotelServiceRepository; 

	@Override
	public Hotel getOne(String code) {
		return hotelRepository.findByhotelId(code);
	}
	@Override
	public HotelServices findHotelServiceByHotelId(String hotelId) {
		return hotelServiceRepository.findHotelServiceByHotelId(hotelId, Constants.SERVICE_NAME);
	}
}
