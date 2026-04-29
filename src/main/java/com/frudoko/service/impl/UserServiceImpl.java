package com.frudoko.service.impl;

import com.frudoko.DAO.UserDAO;
import com.frudoko.errors.ServiceResult;
import com.frudoko.model.User;
import com.frudoko.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDAO;

    private final BCryptPasswordEncoder bCrypt =
            new BCryptPasswordEncoder(12);

    // ---------------- REGISTER ----------------
    @Override
    public ServiceResult<User> register(User user) {
        try {

            if (userDAO.findByEmail(user.getEmail()) != null) {
                return new ServiceResult<>(false, "Email already exists");
            }

            if (userDAO.findByUserName(user.getUserName()) != null) {
                return new ServiceResult<>(false, "Username already exists");
            }

            user.setPassword(bCrypt.encode(user.getPassword()));
            userDAO.save(user);

            return new ServiceResult<>(true, "Registration successful", user);

        } catch (Exception e) {
            return error();
        }
    }

    // ---------------- LOGIN ----------------
    @Override
    public ServiceResult<User> login(String email, String password) {

        try {
            User user = userDAO.findByEmail(email);

            if (user == null || !bCrypt.matches(password, user.getPassword())) {
                return new ServiceResult<>(false, "Invalid credentials");
            }

            return new ServiceResult<>(true, "Login successful", user);

        } catch (Exception e) {
            return error();
        }
    }

    // ---------------- DELETE ----------------
    @Override
    public ServiceResult<Void> delete(int id) {
        try {

            User user = userDAO.findById(id);

            if (user == null) {
                return new ServiceResult<>(false, "User not found");
            }

            userDAO.delete(id);

            return new ServiceResult<>(true, "User deleted successfully");

        } catch (Exception e) {
            return error();
        }
    }

    // ---------------- GET BY ID ----------------
    @Override
    public ServiceResult<User> getById(int id) {
        try {

            User user = userDAO.findById(id);

            if (user == null) {
                return new ServiceResult<>(false, "User not found");
            }

            return new ServiceResult<>(true, "User found", user);

        } catch (Exception e) {
            return error();
        }
    }

    // ---------------- EXISTS USERNAME ----------------
    public boolean existsByUserName(String userName) {
        return userDAO.findByUserName(userName) != null;
    }

    // ---------------- HELPER ----------------
    private <T> ServiceResult<T> error() {
        return new ServiceResult<>(false, "Server error");
    }
}