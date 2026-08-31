package com.technical.interview.project_interview.service;

import com.technical.interview.project_interview.client.PetstoreClient;
import com.technical.interview.project_interview.client.PetstorePet;
import com.technical.interview.project_interview.dto.CreatePetRequest;
import com.technical.interview.project_interview.dto.CreatePetResponse;
import com.technical.interview.project_interview.dto.GetPetResponse;
import com.technical.interview.project_interview.exception.InvalidPetRequestException;
import com.technical.interview.project_interview.exception.PetNotFoundException;
import com.technical.interview.project_interview.exception.PetstoreUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetstoreClient petstoreClient;

    private PetService petService;

    @BeforeEach
    void setUp() {
        petService = new PetService(petstoreClient);
    }

    // --- GET ---

    @Test
    void getPet_returnsThePetMappedToOurContract() {
        when(petstoreClient.getPet(123L)).thenReturn(new PetstorePet(123L, "kitty", "available"));

        GetPetResponse response = petService.getPet(123L);

        assertThat(response).isEqualTo(new GetPetResponse(123L, "kitty", "available"));
    }

    @Test
    void getPet_propagatesPetNotFoundExceptionWithoutWrapping() {
        when(petstoreClient.getPet(999L)).thenThrow(new PetNotFoundException(999L, new RuntimeException()));

        assertThatThrownBy(() -> petService.getPet(999L))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void getPet_propagatesPetstoreUnavailableExceptionWithoutWrapping() {
        when(petstoreClient.getPet(1L)).thenThrow(new PetstoreUnavailableException("timeout", new RuntimeException()));

        assertThatThrownBy(() -> petService.getPet(1L))
                .isInstanceOf(PetstoreUnavailableException.class);
    }

    // --- POST ---

    @Test
    void createPet_sendsToPetstoreThePetBuiltFromTheRequest() {
        CreatePetRequest request = new CreatePetRequest(10L, "rex", "available");
        when(petstoreClient.createPet(any())).thenReturn(new PetstorePet(10L, "rex", "available"));

        petService.createPet(request);

        verify(petstoreClient).createPet(new PetstorePet(10L, "rex", "available"));
    }

    @Test
    void createPet_buildsTheResponseWithWhatPetstoreConfirms() {
        CreatePetRequest request = new CreatePetRequest(10L, "rex", "available");
        when(petstoreClient.createPet(any())).thenReturn(new PetstorePet(10L, "rex", "available"));

        CreatePetResponse response = petService.createPet(request);

        assertThat(response.name()).isEqualTo("rex");
        assertThat(response.status()).isEqualTo("available");
    }

    @Test
    void createPet_generatesATransactionIdAsUuidV4() {
        when(petstoreClient.createPet(any())).thenReturn(new PetstorePet(10L, "rex", "available"));

        CreatePetResponse response = petService.createPet(new CreatePetRequest(10L, "rex", "available"));

        assertThat(response.transactionId()).isNotNull();
        assertThat(response.transactionId().version()).isEqualTo(4);
    }

    @Test
    void createPet_generatesDateCreatedWithTheCurrentSystemDate() {
        when(petstoreClient.createPet(any())).thenReturn(new PetstorePet(10L, "rex", "available"));

        LocalDateTime before = LocalDateTime.now();
        CreatePetResponse response = petService.createPet(new CreatePetRequest(10L, "rex", "available"));
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.dateCreated()).isBetween(before, after);
    }

    @Test
    void createPet_propagatesPetstoreUnavailableExceptionWithoutWrapping() {
        when(petstoreClient.createPet(any())).thenThrow(new PetstoreUnavailableException("caido", new RuntimeException()));

        assertThatThrownBy(() -> petService.createPet(new CreatePetRequest(1L, "rex", "available")))
                .isInstanceOf(PetstoreUnavailableException.class);
    }

    @Test
    void createPet_rejectsRequestWithBlankNameWithoutCallingPetstore() {
        CreatePetRequest request = new CreatePetRequest(1L, "", "available");

        assertThatThrownBy(() -> petService.createPet(request))
                .isInstanceOf(InvalidPetRequestException.class);
    }
}
