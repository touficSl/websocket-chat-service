package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.ts.messenger.entities.User;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	
	@Query(value = "FROM User WHERE userLogin = :hotelUsername ", nativeQuery = false)
	User getByUserLogin(@Param("hotelUsername") String hotelUsername);
    
}