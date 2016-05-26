package kr.tinywind.reactpracticeweb;

import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Launcher {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Launcher.class, args);
        UserRepository userRepository = context.getBean(UserRepository.class);
    }
}