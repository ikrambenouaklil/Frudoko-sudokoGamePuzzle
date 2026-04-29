//package com.frudoko.service;
//
//
//import com.frudoko.DAO.GameStateRepository;
//import com.frudoko.DAO.UserRepository;
//import com.frudoko.model.GameState;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.time.LocalDateTime;
//
//@Service
//@Transactional
//public class GameStateService {
//    @Autowired
//    private UserRepository userRepo ; 
//    @Autowired
//    private GameStateRepository repo;
//    @Autowired
//    private SudokuGenerator generator;
//public GameState createGame (int userId , String level){
//    
//    int [][] solution = generator.generateSolution();
//    int [][] puzzle = generator.generatePuzzle(solution , level );
//    
//
//    GameState gs = new GameState();
//    gs.setUser(userRepo.findById(userId).orElseThrow());
//    gs.setLevel(GameState.Level.valueOf(level));
//    gs.setSolution(gridToJson(solution));
//    gs.setInitialPuzzle(gridToJson(puzzle));
//    gs.setGridState(gridToJson(puzzle));
//    gs.setTimeElapsed(0);
//    gs.setCreatedAt(LocalDateTime.now());
//    gs.setUpdatedAt(LocalDateTime.now());
//    return repo.save(gs);
//}
//
///** ----------------------------- helpers--------------- */
//    public String gridToJson(int[][] grid) {
//        StringBuilder sb = new StringBuilder("[");
//        for (int r = 0; r < 4; r++) {
//            sb.append("[");
//            for (int c = 0; c < 4; c++) {
//                sb.append(grid[r][c]);
//                if (c < 3) sb.append(",");
//            }
//            sb.append("]");
//            if (r < 3) sb.append(",");
//        }
//        return sb.append("]").toString();
//    }
//}
