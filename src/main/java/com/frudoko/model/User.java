package com.frudoko.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;


import javax.persistence.*;



@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_name",nullable = false, unique = true)
    private String userName;
    @Column(nullable = false)
    private String password;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scores;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameState> gameStates;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public List<GameState> getGameStates() {
        return gameStates;
    }

    public void setGameStates(List<GameState> gameStates) {
        this.gameStates = gameStates;
    }
}
