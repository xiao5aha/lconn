package com.huo.lconn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LconnApplication {

    public static void main(String[] args) {
        SpringApplication.run(LconnApplication.class, args);
    }

}
