package kr.tinywind.springbootstreaming.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PostInitActionConfig implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(PostInitActionConfig.class);

    @Override
    public void run(String... strings) throws Exception {
    }
}