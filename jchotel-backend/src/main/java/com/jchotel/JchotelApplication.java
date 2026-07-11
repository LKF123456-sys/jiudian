package com.jchotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JchotelApplication {
    public static void main(String[] args) {
        SpringApplication.run(JchotelApplication.class, args);
    }
}
