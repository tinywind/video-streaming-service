package kr.tinywind.reactpracticeweb.controller;

import kr.tinywind.reactpracticeweb.model.User;
import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class SimpleController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/add")
    public User add(User hello) {
        User user = userRepository.save(hello);
        return user;
    }

    @RequestMapping("/list")
    public Iterable<User> list(Model model) {
        Iterable<User> userList = userRepository.findAll();
        return userList;
    }

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "User World!";
    }

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(SimpleController.class, args);
    }
}