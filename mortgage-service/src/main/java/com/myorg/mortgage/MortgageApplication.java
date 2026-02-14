package com.myorg.mortgage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(excludeName = {
//        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
//        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
//})
@SpringBootApplication
public class MortgageApplication {

    static void main(String[] args) {
        SpringApplication.run(MortgageApplication.class);
    }
}
