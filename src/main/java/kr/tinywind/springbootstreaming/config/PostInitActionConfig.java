package kr.tinywind.springbootstreaming.config;

import kr.tinywind.springbootstreaming.model.User;
import kr.tinywind.springbootstreaming.model.UserRepository;
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

    @Override
    public void run(String... strings) throws Exception {
        User saved = userRepository.save(new User("Jeon", "JaeHyeong", new Date()));
        logger.debug(saved.toString());
    }
}