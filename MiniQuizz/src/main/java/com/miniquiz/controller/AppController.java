package com.miniquiz.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.miniquiz.model.User;
@Controller
public class AppController {

    @Value("${aplicacion.nombre:MiniQuiz Live}")
    private String titulo;

    /*@GetMapping("/")
    public String inicio(Model model) {
        System.out.println("index: " + titulo);
        model.addAttribute("titulo", titulo);
        return "index";
    }*/
    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null) return "redirect:/login";

        model.addAttribute("titulo", titulo);
        model.addAttribute("username", u.getUsername());
        return "index";
    }

}
