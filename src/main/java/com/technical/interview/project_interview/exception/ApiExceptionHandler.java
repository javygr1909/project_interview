package com.technical.interview.project_interview.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ApiError> handlePetNotFound(PetNotFoundException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(PetstoreUnavailableException.class)
    public ResponseEntity<ApiError> handlePetstoreUnavailable(PetstoreUnavailableException ex) {
        log.error("Fallo al comunicarse con Petstore", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError("El servicio de Petstore no está disponible"));
    }

    @ExceptionHandler(InvalidPetRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidPetRequestException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn(ex.getMessage());
        String message = "El parámetro '" + ex.getName() + "' tiene un formato inválido";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("El body de la petición no es un JSON válido"));
    }
}
