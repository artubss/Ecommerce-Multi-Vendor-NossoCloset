package com.zosh.service;

import com.zosh.exception.UserException;
import com.zosh.model.User;

public interface UserService {

    public User findUserProfileByJwt(String jwt) throws UserException;

    public User findUserByEmail(String email) throws UserException;

}
