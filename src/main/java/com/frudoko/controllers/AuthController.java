//package com.frudoko.controllers;
//
//import com.frudoko.errors.ServiceResult;
//import com.frudoko.model.User;
//import com.frudoko.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import javax.servlet.http.HttpSession;
//
//@Controller
//public class AuthController {
//    @Autowired
//    private UserService userSvc;
//    
//    
//    // get login page 
//    @GetMapping("/login")
//    public String loginPage(HttpSession session) {
//        if (session.getAttribute("user") != null)
//            return "redirect:/levels";
//        return "login";
//    }
//
//
// @PostMapping("/login")
//    public String doLogin (@RequestParam String identifier ,
//                            @RequestParam String password ,
//                           HttpSession session ,
//                           Model model){
//          ServiceResult <User> result = userSvc.login(identifier, password);
//     if (result.isSuccess()) {
//         session.setAttribute("user", result.getData());
//         return "redirect:/levels";
//     }
//     model.addAttribute("error", result.getMessage());
//     return "login";
// }
//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        session.invalidate();
//        return "redirect:/home";
//    }
//    // ── Register ──────────────────────────────────────────────────
//
//    @GetMapping("/register")
//    public String registerPage(HttpSession session) {
//        if (session.getAttribute("user") != null)
//            return "redirect:/levels";
//        return "register";
//    }
//    @PostMapping("/register")
//    public String doRegister(@RequestParam String username,
//                             @RequestParam String email,
//                             @RequestParam String password,
//                             Model model) {
//
//        // تحقق من الـ email
//        if (userSvc.existsByEmail(email)) {
//            model.addAttribute("error", "Cet email est déjà utilisé.");
//            return "register";
//        }
//
//        // تحقق من الـ username
//        if (userSvc.existsByUserName(username)) {
//            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris.");
//            return "register";
//        }
//
//        // خلق اليوزر
//        User user = new User();
//        user.setUserName(username);
//        user.setEmail(email);
//        user.setPassword(password); // register() تعمل الـ hash تلقائياً
//
//        // ✅ register() بدل save()
//        ServiceResult<User> result = userSvc.register(user);
//
//        if (!result.isSuccess()) {
//            model.addAttribute("error", result.getMessage());
//            return "register";
//        }
//
//        return "redirect:/login?registered=true";
//    }
//}
