package com.technical.interview.project_interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePetResponse(
    UUID transactionId,
    LocalDateTime dateCreated, 
    String status, 
    String name) {
}
