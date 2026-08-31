package com.technical.interview.project_interview.dto;

public record CreatePetRequest(
    Long id, 
    String name,
    String status) {
}
