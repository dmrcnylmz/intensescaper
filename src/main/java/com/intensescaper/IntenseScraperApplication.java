package com.intensescaper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class IntenseScraperApplication implements org.springframework.boot.CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(IntenseScraperApplication.class, args);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.intensescaper.service.browser.PlaywrightStealthClient stealthClient;

    @Override
    public void run(String... args) throws Exception {
        for (String arg : args) {
            if ("--prime".equals(arg)) {
                stealthClient.primeSession();
                System.exit(0);
            }
        }
    }

}
