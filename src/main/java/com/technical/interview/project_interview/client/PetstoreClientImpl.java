package com.technical.interview.project_interview.client;

import com.technical.interview.project_interview.exception.PetNotFoundException;
import com.technical.interview.project_interview.exception.PetstoreUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class PetstoreClientImpl implements PetstoreClient {

    private final RestClient restClient;

    public PetstoreClientImpl(RestClient.Builder restClientBuilder, PetstoreProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public PetstorePet getPet(Long petId) {
        try {
            return restClient.get()
                    .uri("/pet/{petId}", petId)
                    .retrieve()
                    .body(PetstorePet.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PetNotFoundException(petId, e);
        } catch (RestClientResponseException e) {
            throw new PetstoreUnavailableException("Petstore respondió con error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new PetstoreUnavailableException("No se pudo conectar con Petstore", e);
        }
    }

    @Override
    public PetstorePet createPet(PetstorePet pet) {
        try {
            return restClient.post()
                    .uri("/pet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pet)
                    .retrieve()
                    .body(PetstorePet.class);
        } catch (RestClientResponseException e) {
            throw new PetstoreUnavailableException("Petstore respondió con error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new PetstoreUnavailableException("No se pudo conectar con Petstore", e);
        }
    }
}
