package com.ts.messenger.services;

import com.ts.messenger.entities.Timezone;

public interface TimezoneService {
	Timezone getTimezoneById(String id);
}