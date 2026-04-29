package com.frudoko.controllers;


import com.frudoko.DAO.GameStateDAO;
import com.frudoko.DAO.ScoreDAO;
import com.frudoko.model.Score;
import com.frudoko.model.User;
import com.frudoko.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * HomeController — handles landing page and scoreboard.
 * Routes: /home, /, /scoreboard
 */
@Controller
public class HomeController {

    @Autowired
    private ScoreDAO scoreDAO;

    /** Landing page — shows Login / Register buttons */
    @GetMapping({"/", "/home"})
    public String home(HttpSession session) {
//        // If already logged in, redirect to levels
//        if (session.getAttribute("user") != null) {
//            return "redirect:/levels";
//        }
        return "error404";
    }
//
//    /** Global scoreboard — top 20 scores */
//    @GetMapping("/scoreboard")
//    public String scoreboard(Model model) {
//        List<Score> easy = scoreDAO.findTopScoresByLevel("EASY", 20);
//
//        System.out.println("EASY scores: " + easy.size());
//        model.addAttribute("scoresEasy",   scoreDAO.findTopScoresByLevel("EASY",   20));
//        model.addAttribute("scoresMedium", scoreDAO.findTopScoresByLevel("MEDIUM", 20));
//        model.addAttribute("scoresHard",   scoreDAO.findTopScoresByLevel("HARD",   20));
//        return "scoreboard";
//    }
//
//    @GetMapping("/levels")
//    public String levelsPage(HttpSession session, Model model) {
//
//        // تحققي من الـ session
//        User user = (User) session.getAttribute("user");
//        if (user == null) return "redirect:/login";
//
//        // هنا تحطي user و totalScore باش navbar يشوفهم
//        model.addAttribute("user", user);
//        model.addAttribute("totalScore", scoreDAO.sumPointsByUserId(user.getId()));
//
//        return "levels";
//    }


}
