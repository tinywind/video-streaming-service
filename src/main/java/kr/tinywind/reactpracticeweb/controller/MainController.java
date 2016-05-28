package kr.tinywind.reactpracticeweb.controller;

import kr.tinywind.reactpracticeweb.service.React;
import kr.tinywind.reactpracticeweb.model.User;
import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.script.ScriptException;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private React react;

    @RequestMapping("/add")
    @ResponseBody
    public User add(User hello) {
        User user = userRepository.save(hello);
        return user;
    }

    @RequestMapping("/list")
    @ResponseBody
    public Iterable<User> list(Model model) {
        Iterable<User> userList = userRepository.findAll();
        return userList;
    }

    @RequestMapping("/react-server-side-test")
    public String reactServerSideTestPage(Model model) throws ScriptException {
        model.addAttribute("content", react.render("CommentBox", "url", "/", "pollInterval", "5000"));
        return "react-server-side-test";
    }

    @RequestMapping("/")
    public String homePage(Model model) {
        return "home";
    }
}