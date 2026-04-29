package com.frudoko.DAO.impl;

import com.frudoko.DAO.UserDAO;
import com.frudoko.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class UserDAOImpl implements UserDAO {

// create and delete 

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(User user) {

        if (user.getId() == 0) {
            em.persist(user);
        } else {
            em.merge(user);
        }
       
    }

    @Override
    public void delete(int id) {
    User user =  em.find(User.class, id);
     em.remove (user); 
    }
// ------------ find ----------------
    @Override
    public User findByEmail(String email) {

        String jpql = "SELECT u FROM User u WHERE u.email = :email";
        return em.createQuery(jpql, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);


       
    }

    public User findByUserName(String userName) {

        String jpql = "SELECT u FROM User u WHERE u.userName = :userName";
        return em.createQuery(jpql, User.class)
                .setParameter("userName", userName)
                .getResultStream()
                .findFirst()
                .orElse(null);
        
    }

    public User findById(int id) {
        
        return em.find(User.class, id);
    }


}
