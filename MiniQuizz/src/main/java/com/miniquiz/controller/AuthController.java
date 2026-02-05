package com.miniquiz.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.miniquiz.model.User;
import com.miniquiz.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // --- LOGIN ---
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String doLogin(
            @ModelAttribute("user") User user,
            Model model,
            HttpSession session) {

        Optional<User> uOpt = userService.login(user.getUsername(), user.getPassword());

        if (uOpt.isPresent()) {
            session.setAttribute("user", uOpt.get());
            return "redirect:/";
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        model.addAttribute("user", new User());
        return "login";
    }

    // --- LOGOUT ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- REGISTER ---
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        // comprobar username repetido
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Ese usuario ya existe");
            return "register";
        }

        User saved = userService.registerUser(user);

        // auto-login después de registrarse
        session.setAttribute("user", saved);
        return "redirect:/";
    }
}
