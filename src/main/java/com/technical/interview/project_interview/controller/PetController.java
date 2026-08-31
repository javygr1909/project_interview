package com.technical.interview.project_interview.controller;

import com.technical.interview.project_interview.dto.CreatePetRequest;
import com.technical.interview.project_interview.dto.CreatePetResponse;
import com.technical.interview.project_interview.dto.GetPetResponse;
import com.technical.interview.project_interview.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/{petId}")
    public GetPetResponse getPet(@PathVariable Long petId) {
        return petService.getPet(petId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePetResponse createPet(@RequestBody CreatePetRequest request) {
        return petService.createPet(request);
    }
}
