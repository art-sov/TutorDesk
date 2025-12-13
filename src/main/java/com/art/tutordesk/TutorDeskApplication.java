package com.art.tutordesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TutorDeskApplication {

	static void main(String[] args) {
		SpringApplication.run(TutorDeskApplication.class, args);
	}
}
