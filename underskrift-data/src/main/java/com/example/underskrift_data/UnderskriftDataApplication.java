package com.example.underskrift_data;

import com.example.artemis_configuration.EnableArtemis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableArtemis
public class UnderskriftDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnderskriftDataApplication.class, args);
	}

}
