package es.e1sordo.lingualeap;

import es.e1sordo.lingualeap.configuration.DatabaseMigrationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LinguaLeapApplication {

    public static void main(String[] args) {
        DatabaseMigrationRunner.runFromEnvironment();
        SpringApplication.run(LinguaLeapApplication.class, args);
    }
}
