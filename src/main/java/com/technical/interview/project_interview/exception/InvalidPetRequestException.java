package com.technical.interview.project_interview.exception;

public class InvalidPetRequestException extends RuntimeException {

    public InvalidPetRequestException(String message) {
        super(message);
    }
}
