package com.frudoko.DAO.impl;

import com.frudoko.DAO.GameStateDAO;
import com.frudoko.model.GameState;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
@Repository
public class GameStateDAOImpl implements GameStateDAO {

    @PersistenceContext
    private EntityManager em;
    @Override
    public void save(GameState gs) {
        if(gs.getId() == 0 ){
            em.persist(gs); 
        }else{
            em.merge(gs); 
        }

    }

    @Override
    public void delete(int id) {
        GameState gs = em.find(GameState.class , id); 
        if (gs != null) em.remove (gs);
    }

    @Override
    public GameState findById(int id) {
        return em.find(GameState.class , id);
    }

    @Override
    public GameState findByUserIdAndStatus(int userId, GameState.GameStatus status) {
        String jpql =
                "SELECT g FROM GameState g WHERE g.user.id = :userId AND g.status = :status " +
                        "ORDER BY g.updatedAt DESC";
        return em.createQuery(jpql,GameState.class)
                .setParameter("userId", userId)
                .setParameter("status",status)
                .setMaxResults(1)
                .getResultStream()
                .findFirst().orElse(null); 


    }
}
