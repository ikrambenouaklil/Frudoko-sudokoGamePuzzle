package com.frudoko.DAO.impl;

import com.frudoko.DAO.ScoreDAO;
import com.frudoko.model.Score;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
@Repository
public class ScoreDAOImpl  implements ScoreDAO {
    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(Score score) {
            em.persist(score);
    }

    @Override
    public List<Score> findByUserId(int userId) {
        String jpql = 
        "SELECT s FROM Score s WHERE s.user.id = :userId ORDER BY s.playedAt DESC";
        return em.createQuery(jpql , Score.class)
                .setParameter("userId",userId)
                .getResultList();

        
    }

    @Override
    public int sumPointsByUserId(int userId) {
        String jpql = "SELECT COALESCE(SUM(s.points), 0) FROM Score s WHERE s.user.id = :userId";
        Long result = em.createQuery(jpql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        return result.intValue();
    }

    @Override
    public List<Score> findTopScores(int limit) {
        String jpql = "SELECT s FROM Score s ORDER BY s.points DESC";
        return em.createQuery(jpql, Score.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
