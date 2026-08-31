package com.technical.interview.project_interview.exception;

public class PetNotFoundException extends RuntimeException {

    public PetNotFoundException(Long petId, Throwable cause) {
        super("No se encontró el pet con id " + petId, cause);
    }
}
