package com.frudoko.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null)
            return "redirect:/levels";
        return "login";
    }
}
