package com.example.Boggle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.bogglespringboot",
        "com.example.Boggle"
})
public class BoggleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoggleApplication.class, args);
    }
}
