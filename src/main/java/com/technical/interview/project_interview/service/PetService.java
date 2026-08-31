package com.technical.interview.project_interview.service;

import com.technical.interview.project_interview.client.PetstoreClient;
import com.technical.interview.project_interview.client.PetstorePet;
import com.technical.interview.project_interview.dto.CreatePetRequest;
import com.technical.interview.project_interview.dto.CreatePetResponse;
import com.technical.interview.project_interview.dto.GetPetResponse;
import com.technical.interview.project_interview.exception.InvalidPetRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetstoreClient petstoreClient;

    public PetService(PetstoreClient petstoreClient) {
        this.petstoreClient = petstoreClient;
    }

    public GetPetResponse getPet(Long petId) {
        PetstorePet pet = petstoreClient.getPet(petId);
        log.info("Pet obtenido de Petstore: {}", pet);
        return toGetPetResponse(pet);
    }

    public CreatePetResponse createPet(CreatePetRequest request) {
        validate(request);
        PetstorePet petToCreate = new PetstorePet(request.id(), request.name(), request.status());
        PetstorePet createdPet = petstoreClient.createPet(petToCreate);
        log.info("Pet creado en Petstore: {}", createdPet);
        return toCreatePetResponse(createdPet);
    }

    private static void validate(CreatePetRequest request) {
        if (request.id() == null) {
            throw new InvalidPetRequestException("id es obligatorio");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidPetRequestException("name es obligatorio");
        }
        if (request.status() == null || request.status().isBlank()) {
            throw new InvalidPetRequestException("status es obligatorio");
        }
    }

    private static GetPetResponse toGetPetResponse(PetstorePet pet) {
        return new GetPetResponse(pet.id(), pet.name(), pet.status());
    }

    private static CreatePetResponse toCreatePetResponse(PetstorePet pet) {
        return new CreatePetResponse(UUID.randomUUID(), LocalDateTime.now(), pet.status(), pet.name());
    }
}
