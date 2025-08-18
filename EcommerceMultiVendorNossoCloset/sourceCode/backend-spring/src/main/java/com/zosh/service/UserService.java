package com.nossocloset.service;

import com.nossocloset.exception.UserException;
import com.nossocloset.model.User;

public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;


}
