package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.ts.messenger.entities.Timezone;

@RepositoryRestResource
public interface TimezoneRepository extends JpaRepository<Timezone, String> {

	Timezone getTimezoneById(String id);
}
