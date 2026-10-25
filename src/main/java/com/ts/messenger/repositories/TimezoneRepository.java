package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.Timezone;

@Repository
public interface TimezoneRepository extends JpaRepository<Timezone, String> {
	Timezone getTimezoneById(String id);
}
