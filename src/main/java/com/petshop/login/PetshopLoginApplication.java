package com.petshop.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.petshop.login")
public class PetshopLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetshopLoginApplication.class, args);
	}

}
