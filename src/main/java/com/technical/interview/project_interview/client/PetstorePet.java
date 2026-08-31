package com.technical.interview.project_interview.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PetstorePet(
    Long id, 
    String name, 
    String status) {
}
