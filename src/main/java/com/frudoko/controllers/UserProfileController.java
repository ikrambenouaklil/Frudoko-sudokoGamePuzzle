package com.frudoko.controllers;

import com.frudoko.model.User;
import com.frudoko.service.UserService;
import com.frudoko.errors.ServiceResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * UserProfileController — AJAX endpoints for the profile modal.
 */
@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService userSvc;

    // ── Update username + email ───────────────────────────────────

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam String username,
            @RequestParam String email,
            HttpSession session) {

        Map<String, Object> resp = new HashMap<>();
        User user = (User) session.getAttribute("user");

        // Check session
        if (user == null) {
            resp.put("status", "unauthorized");
            return ResponseEntity.status(401).body(resp);
        }

        // Check email uniqueness ONLY if changed
        if (!email.equals(user.getEmail()) && userSvc.existsByEmail(email)) {
            resp.put("status", "error");
            resp.put("message", "Cet email est déjà utilisé.");
            return ResponseEntity.badRequest().body(resp);
        }

        // Check username uniqueness ONLY if changed
        if (!username.equals(user.getUserName()) && userSvc.existsByUserName(username)) {
            resp.put("status", "error");
            resp.put("message", "Username already taken.");
            return ResponseEntity.badRequest().body(resp);
        }

        // Apply changes
        user.setUserName(username);
        user.setEmail(email);
        userSvc.edit(user);

        // Refresh session (important)
        ServiceResult<User> updated = userSvc.getById(user.getId());
        session.setAttribute("user", updated.getData());

        resp.put("status", "ok");
        resp.put("username", username);

        return ResponseEntity.ok(resp);
    }

    // ── Change password ───────────────────────────────────────────

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam String password,
            HttpSession session) {

        Map<String, Object> resp = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.put("status", "unauthorized");
            return ResponseEntity.status(401).body(resp);
        }

        // Basic validation
        if (password.length() < 6) {
            resp.put("status", "error");
            resp.put("message", "Le mot de passe doit contenir au moins 6 caractères.");
            return ResponseEntity.badRequest().body(resp);
        }

        userSvc.changePassword(user.getId(), password);

        resp.put("status", "ok");
        return ResponseEntity.ok(resp);
    }

    // ── Delete account ────────────────────────────────────────────

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(HttpSession session) {

        Map<String, Object> resp = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.put("status", "unauthorized");
            return ResponseEntity.status(401).body(resp);
        }

        userSvc.delete(user.getId());
        session.invalidate();

        resp.put("status", "deleted");
        resp.put("redirect", "/home");

        return ResponseEntity.ok(resp);
    }
}

