package com.frudoko.service;

import com.frudoko.model.GameState;

public interface GameStateService {
    
    GameState getOrCreateGame(int userId, String level);
    void saveProgress(int gameId, String gridJson, long elapsed);
    GameState checkWin(int gameId, String gridJson);
    void abandonGame(int userId);
    int[][] parseGrid(String json);
    boolean[][] buildFixedMask(int[][] puzzle);


}
