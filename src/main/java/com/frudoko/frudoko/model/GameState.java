package com.frudoko.model;


import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * GameState — persists the complete state of a player's game session.
 *
 * Grid encoding:  0 = empty cell (player must fill)
 *                 1 = 🍎  2 = 🍌  3 = 🍇  4 = 🍊
 *
 * gridState and solution are stored as JSON: [[1,2,3,4],[...],...]
 */

@Entity
@Table(name = "game_states")
public class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** References users.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Difficulty level */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    /** Current board state as JSON 4x4 array — updated on every auto-save */
    @Column(name = "grid_state", columnDefinition = "TEXT", nullable = false)
    private String gridState;

    /** Complete correct solution as JSON 4x4 array — never sent to client */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String solution;

    /** Elapsed time in seconds — persisted for resume */
    @Column(name = "time_elapsed")
    private long timeElapsed;

    /** Current game status */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "initial_puzzle", columnDefinition = "TEXT", nullable = false)
    private String initialPuzzle;
    // ── Enums ──────────────────────────────────────────────────────

    public enum Level {
        EASY, MEDIUM, HARD
    }

    public enum GameStatus {
        IN_PROGRESS, WON, LOST
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getGridState() {
        return gridState;
    }

    public void setGridState(String gridState) {
        this.gridState = gridState;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getInitialPuzzle() {
        return initialPuzzle;
    }

    public void setInitialPuzzle(String initialPuzzle) {
        this.initialPuzzle = initialPuzzle;
    }
}
