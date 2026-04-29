package com.frudoko.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * HomeController — handles landing page and scoreboard.
 * Routes: /home, /, /scoreboard
 */
@Controller
public class HomeController {

//    @Autowired
//    private GameService gameService;

    /** Landing page — shows Login / Register buttons */
    @GetMapping({"/", "/home"})
    public String home(HttpSession session) {
        // If already logged in, redirect to levels
        if (session.getAttribute("user") != null) {
            return "redirect:/levels";
        }
        return "home";
    }

    /** Global scoreboard — top 20 scores */
//    @GetMapping("/scoreboard")
//    public String scoreboard(Model model) {
//        model.addAttribute("scores", gameService.getTopScores(20));
//        return "scoreboard";
//    }
}
