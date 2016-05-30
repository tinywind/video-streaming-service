package kr.tinywind.springbootstreaming.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {
    @RequestMapping("/")
    public String homePage(Model model) {
        model.addAttribute("name", "Jeon");
        return "home";
    }
}