package com.frivlegion.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FrivLegionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrivLegionApplication.class, args);
	}

}
