package com.fitness.activityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class ActivityserviceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ActivityserviceApplication.class, args);
	}

}

// while adding eureka client add version in properties tag of pm.xml and dependencies management just below the dependencies  and build
