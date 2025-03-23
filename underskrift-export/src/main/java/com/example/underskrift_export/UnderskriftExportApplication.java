package com.example.underskrift_export;

import com.example.artemis_configuration.EnableArtemis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableArtemis
public class UnderskriftExportApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnderskriftExportApplication.class, args);
	}

}
