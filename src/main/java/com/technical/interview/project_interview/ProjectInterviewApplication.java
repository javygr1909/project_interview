package com.technical.interview.project_interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProjectInterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectInterviewApplication.class, args);
	}

}
