package com.frudoko.DAO;


import com.frudoko.model.Score;

import java.util.List;

public interface ScoreDAO  {
    void save(Score score);
    List<Score> findByUserId(int userId);
    int sumPointsByUserId(int userId);
    List<Score> findTopScores(int limit);

}