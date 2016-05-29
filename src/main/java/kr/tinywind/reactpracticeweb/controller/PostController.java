package kr.tinywind.reactpracticeweb.controller;

import kr.tinywind.reactpracticeweb.model.Post;
import kr.tinywind.reactpracticeweb.model.PostRepository;
import kr.tinywind.reactpracticeweb.service.React;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @RequestMapping(value = "write", method = RequestMethod.GET)
    public String form(@ModelAttribute Post post) {
        return "post/form";
    }

    @RequestMapping(value = "write", method = RequestMethod.POST)
    public String add(Post post, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // alert();
            return "redirect:/post";
        }
        post.setCreatedAt(new Date());
        return "redirect:/post/" + postRepository.save(post).getId();
    }

    @RequestMapping(value = "{id}/modify", method = RequestMethod.GET)
    public String modify(Model model, @PathVariable("id") Long id) {
        model.addAttribute("post", postRepository.findOne(id));
        return "post/form";
    }

    @RequestMapping(value = "{id}/modify", method = RequestMethod.POST)
    public String edit(Post post, BindingResult bindingResult, @PathVariable("id") Long id) {
        if (bindingResult.hasErrors()) {
            // alert();
            return "redirect:/post";
        }
        post.setId(id);
        return "redirect:/post/" + postRepository.save(post).getId();
    }

    @RequestMapping
    public String listPage(Model model, @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 5) Pageable pageable) {
        model.addAttribute("posts", postRepository.findAll(pageable));
        return "post/list";
    }

    @RequestMapping("{id}")
    public String page(Model model, @PathVariable("id") Long id) {
        model.addAttribute("post", postRepository.findOne(id));
        return "post/post";
    }

    @RequestMapping("{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        postRepository.delete(id);
        return "redirect:/post";
    }
}