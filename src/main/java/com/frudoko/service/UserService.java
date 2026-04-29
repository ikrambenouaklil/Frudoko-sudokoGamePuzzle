package com.frudoko.service;

import com.frudoko.errors.ServiceResult;
import com.frudoko.model.User;

public interface UserService {
    ServiceResult<User> register(User user);
    ServiceResult<User> login(String email, String password);
    ServiceResult<Void> delete(int id);
    ServiceResult<User> getById(int id);
    boolean existsByUserName(String userName);
}