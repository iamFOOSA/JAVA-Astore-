package by.abram.astore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstoreApplication.class, args);
    }

}