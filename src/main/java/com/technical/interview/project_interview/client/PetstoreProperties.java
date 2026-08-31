package com.technical.interview.project_interview.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "petstore")
public record PetstoreProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
