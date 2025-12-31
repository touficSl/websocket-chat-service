package com.ts.messenger.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.User;
import com.ts.messenger.repositories.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public User getByUserLogin(String hotelUsername) {
		try {	
			return userRepository.getByUserLogin(hotelUsername);
		} catch (Exception e) {
			return null;
		}
	}
}
