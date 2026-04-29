package com.frudoko.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Score — records the result of each completed game.
 * Used to display the global scoreboard.
 */

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** Points earned = base score - time penalty */
    @Column(nullable = false)
    private int points;

    /** Total game duration in seconds */
    @Column(name = "time_in_seconds")
    private long timeInSeconds;

    /** EASY / MEDIUM / HARD */
    @Column(length = 10)
    private String level;

    @Column(name = "played_at")
    private LocalDateTime playedAt;


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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
