package com.frudoko.DAO;


import com.frudoko.model.GameState;

public interface GameStateDAO {

    void save(GameState gs);
    void delete(int id);
    GameState findById(int id);
    GameState findByUserIdAndStatus(int userId, GameState.GameStatus status);
}
