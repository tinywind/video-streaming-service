package kr.tinywind.reactpracticeweb.service;

import kr.tinywind.reactpracticeweb.model.User;
import kr.tinywind.reactpracticeweb.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final UserRepository repository;

    @Autowired
    public DatabaseLoader(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... strings) throws Exception {
        this.repository.save(new User("Jeon", "JaeHyeong"));
    }
}