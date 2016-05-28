package kr.tinywind.reactpracticeweb.controller;

import kr.tinywind.reactpracticeweb.service.React;
import kr.tinywind.reactpracticeweb.model.Post;
import kr.tinywind.reactpracticeweb.model.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private React react;

    @RequestMapping(value = "/write", method = RequestMethod.GET)
    public String form() {
        return "post/form";
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST)
    public String add(Post post) {
        post.setCreatedAt(new Date());
        return "redirect:/post/" + postRepository.save(post).getId();
    }

    @RequestMapping
    public String listPage(Model model) {
        model.addAttribute("posts", postRepository.findAll());
        return "post/list";
    }

    @RequestMapping("{id}")
    public String page(Model model, @PathVariable("id") Long id) {
        model.addAttribute("post", postRepository.findOne(id));
        return "post/post";
    }
}