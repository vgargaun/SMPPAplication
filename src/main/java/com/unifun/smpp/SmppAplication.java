package com.unifun.smpp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SmppAplication {

    public static void main(String[] args) {
        SpringApplication.run(SmppAplication.class, args);
    }

}
