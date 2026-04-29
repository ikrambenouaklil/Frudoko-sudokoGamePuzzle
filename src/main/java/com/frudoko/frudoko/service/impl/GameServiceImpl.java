package com.frudoko.service.impl;

import com.frudoko.DAO.ScoreDAO;
import com.frudoko.model.Score;
import com.frudoko.model.User;
import com.frudoko.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GameServiceImpl implements GameService {

    @Autowired
    private ScoreDAO scoreDAO;
    @PersistenceContext
    private EntityManager em;


    private static final int BASE_EASY   = 100;
    private static final int BASE_MEDIUM = 200;
    private static final int BASE_HARD   = 350;

    @Override
    public int calculateScore(long elapsedSeconds, String level) {
        int base = switch (level.toUpperCase()) {
            case "HARD"   -> BASE_HARD;
            case "MEDIUM" -> BASE_MEDIUM;
            default       -> BASE_EASY;
        };
        return Math.max(0, base - (int) elapsedSeconds);
    }

    @Override
    public void saveScore(int userId, int points, long elapsed, String level) {
        User user = em.find(User.class, userId);
        Score score = new Score();
        score.setUser(user);
        score.setPoints(points);
        score.setTimeInSeconds(elapsed);
        score.setLevel(level);
        score.setPlayedAt(LocalDateTime.now());
        scoreDAO.save(score);
    }

    @Override
    public List<Score> getUserScores(int userId) {
        return scoreDAO.findByUserId(userId);
    }

    @Override
    public int getTotalScore(int userId) {
        return scoreDAO.sumPointsByUserId(userId);
    }

    @Override
    public String formatTime(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

}