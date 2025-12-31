package com.ts.messenger.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.ts.messenger.authentication.Credentials;
import com.ts.messenger.entities.Timezone;
import com.ts.messenger.repositories.TimezoneRepository;

@Service("timezoneService")
@Repository
@Transactional
public class TimezoneServiceImpl implements TimezoneService {

	@Autowired
	private TimezoneRepository timezoneRepository;

	@Override
	public Timezone getTimezoneById(String id) {
		Timezone timezone = null;
		try {
			timezone = timezoneRepository.findById(id).orElse(null);
		} catch (Exception e) {
			if(Credentials.isDebugMode()) e.printStackTrace();
		}
		return timezone;
	}

}
