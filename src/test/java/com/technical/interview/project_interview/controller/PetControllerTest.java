package com.technical.interview.project_interview.controller;

import com.technical.interview.project_interview.dto.CreatePetRequest;
import com.technical.interview.project_interview.dto.CreatePetResponse;
import com.technical.interview.project_interview.dto.GetPetResponse;
import com.technical.interview.project_interview.exception.InvalidPetRequestException;
import com.technical.interview.project_interview.exception.PetNotFoundException;
import com.technical.interview.project_interview.exception.PetstoreUnavailableException;
import com.technical.interview.project_interview.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    void getPet_returns200WithTheExpectedBody() throws Exception {
        when(petService.getPet(123L)).thenReturn(new GetPetResponse(123L, "kitty", "available"));

        mockMvc.perform(get("/api/pet/{petId}", 123L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.name").value("kitty"))
                .andExpect(jsonPath("$.status").value("available"));
    }

    @Test
    void getPet_returns404WhenTheServiceThrowsPetNotFoundException() throws Exception {
        when(petService.getPet(999L)).thenThrow(new PetNotFoundException(999L, new RuntimeException()));

        mockMvc.perform(get("/api/pet/{petId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el pet con id 999"));
    }

    @Test
    void getPet_returns502WhenTheServiceThrowsPetstoreUnavailableException() throws Exception {
        when(petService.getPet(1L)).thenThrow(new PetstoreUnavailableException("caido", new RuntimeException()));

        mockMvc.perform(get("/api/pet/{petId}", 1L))
                .andExpect(status().isBadGateway());
    }

    @Test
    void getPet_returns400WhenPetIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/pet/{petId}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPet_returns201WithTheExpectedBody() throws Exception {
        CreatePetResponse response = new CreatePetResponse(UUID.randomUUID(), LocalDateTime.now(), "available", "rex");
        when(petService.createPet(new CreatePetRequest(10L, "rex", "available"))).thenReturn(response);

        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":10,"name":"rex","status":"available"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("rex"))
                .andExpect(jsonPath("$.status").value("available"))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.dateCreated").exists());
    }

    @Test
    void createPet_returns400WhenTheServiceThrowsInvalidPetRequestException() throws Exception {
        when(petService.createPet(new CreatePetRequest(1L, "", "available")))
                .thenThrow(new InvalidPetRequestException("name es obligatorio"));

        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":1,"name":"","status":"available"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name es obligatorio"));
    }

    @Test
    void createPet_returns400WhenTheBodyIsNotValidJson() throws Exception {
        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalido"))
                .andExpect(status().isBadRequest());
    }
}
