package kr.tinywind.reactpracticeweb.config;

import kr.tinywind.reactpracticeweb.model.Post;
import kr.tinywind.reactpracticeweb.model.PostRepository;
import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PostInitActionConfig implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(PostInitActionConfig.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public void run(String... strings) throws Exception {
        Post post = new Post();
        post.setTitle("Title");
        post.setSubtitle("Subtitle");
        post.setContent("Content");
        post.setCreatedAt(new Date());
        Post saved = postRepository.save(post);
        logger.debug(saved.toString());
    }
}