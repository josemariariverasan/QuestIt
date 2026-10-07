package com.itcareerquest.itcareerquest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/create-quest")
    public String createQuest() {
        return "create-quest";
    }

    @GetMapping("/quests/{id}")
    public String viewQuest() {
        return "quest";
    }

    @GetMapping("/quests/{id}/edit")
    public String editQuest() {
        return "create-quest";
    }
}