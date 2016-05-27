package kr.tinywind.reactpracticeweb.controller;

import kr.tinywind.reactpracticeweb.config.React;
import kr.tinywind.reactpracticeweb.model.User;
import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.script.ScriptException;

@Controller
public class SimpleController {
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

    @RequestMapping("/")
    public String home(Model model) throws ScriptException {
        model.addAttribute("content", react.render("CommentBox", "url", "/", "pollInterval", "5000"));
        return "hello";
    }
}