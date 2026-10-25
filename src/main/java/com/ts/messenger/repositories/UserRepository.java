package com.ts.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ts.messenger.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value = "FROM User WHERE userLogin = :hotelUsername ", nativeQuery = false)
	User getByUserLogin(@Param("hotelUsername") String hotelUsername);
}
