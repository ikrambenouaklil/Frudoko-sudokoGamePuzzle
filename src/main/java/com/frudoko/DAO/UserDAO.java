package com.frudoko.DAO;

import com.frudoko.model.User;

public interface UserDAO {


    void save(User user);

    void delete(int id);

    User findById(int id);

    User findByEmail(String email);

    User findByUserName(String userName);
   

 
}