package com.ts.messenger.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;
import com.ts.messenger.entities.Timezone;
import com.ts.messenger.repositories.HotelRepository;
import com.ts.messenger.repositories.HotelServiceRepository;
import com.ts.messenger.utils.Constants;

@Service("hotelService")
public class HotelServiceImpl implements HotelService {

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private HotelServiceRepository hotelServiceRepository;

	@Autowired
	private TimezoneService timezoneService;

	@Override
	public Hotel getOne(String code) {
		return hotelRepository.findByhotelId(code);
	}

	@Override
	public HotelServices findHotelServiceByHotelId(String hotelId) {
		return hotelServiceRepository.findHotelServiceByHotelId(hotelId, Constants.SERVICE_NAME);
	}

	@Override
	public String getHotelTimezone(String hotelId) {

		/* Get hotel entity + validation */
		Hotel hotel = getOne(hotelId);
		if (hotel == null) {
			return "-1";
		}

		/* Get hotel timezone */
		Timezone timezone = timezoneService.getTimezoneById(hotel.getTimezone());
		return timezone != null ? timezone.getHotelTimezone() : null;
	}
}
