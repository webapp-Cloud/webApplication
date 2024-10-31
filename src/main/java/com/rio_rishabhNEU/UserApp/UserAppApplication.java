// src/main/java/com/rio_rishabhNEU/UserApp/UserAppApplication.java
package com.rio_rishabhNEU.UserApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.rio_rishabhNEU.UserApp.Model")
@EnableJpaRepositories("com.rio_rishabhNEU.UserApp.DAO")
public class UserAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserAppApplication.class, args);
	}
}