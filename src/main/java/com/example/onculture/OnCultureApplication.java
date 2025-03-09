package com.example.onculture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnCultureApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnCultureApplication.class, args);
	}

}
