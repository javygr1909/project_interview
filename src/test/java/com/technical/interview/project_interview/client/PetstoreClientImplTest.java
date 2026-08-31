package com.technical.interview.project_interview.client;

import com.technical.interview.project_interview.exception.PetNotFoundException;
import com.technical.interview.project_interview.exception.PetstoreUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PetstoreClientImplTest {

    private static final String BASE_URL = "http://petstore.test";

    private MockRestServiceServer mockServer;
    private PetstoreClientImpl petstoreClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        PetstoreProperties properties = new PetstoreProperties(BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(3));
        petstoreClient = new PetstoreClientImpl(builder, properties);
    }

    @Test
    void getPet_returnsThePetIgnoringUnknownFieldsFromPetstore() {
        mockServer.expect(requestTo(BASE_URL + "/pet/123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":123,"name":"kitty","status":"available","category":{"id":1,"name":"cats"},"photoUrls":[]}
                        """, MediaType.APPLICATION_JSON));

        PetstorePet pet = petstoreClient.getPet(123L);

        assertThat(pet).isEqualTo(new PetstorePet(123L, "kitty", "available"));
    }

    @Test
    void getPet_throwsPetNotFoundExceptionWhenPetstoreResponds404() {
        mockServer.expect(requestTo(BASE_URL + "/pet/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Pet not found\"}"));

        assertThatThrownBy(() -> petstoreClient.getPet(999L))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void getPet_throwsPetstoreUnavailableExceptionWhenPetstoreResponds500() {
        mockServer.expect(requestTo(BASE_URL + "/pet/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> petstoreClient.getPet(1L))
                .isInstanceOf(PetstoreUnavailableException.class);
    }

    @Test
    void getPet_throwsPetstoreUnavailableExceptionWhenThereIsNoConnection() {
        mockServer.expect(requestTo(BASE_URL + "/pet/1"))
                .andRespond(request -> {
                    throw new IOException("Connection refused");
                });

        assertThatThrownBy(() -> petstoreClient.getPet(1L))
                .isInstanceOf(PetstoreUnavailableException.class);
    }

    @Test
    void createPet_sendsTheCorrectBodyAndReturnsTheCreatedPet() {
        mockServer.expect(requestTo(BASE_URL + "/pet"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":10,\"name\":\"rex\",\"status\":\"available\"}"))
                .andRespond(withSuccess("""
                        {"id":10,"name":"rex","status":"available"}
                        """, MediaType.APPLICATION_JSON));

        PetstorePet result = petstoreClient.createPet(new PetstorePet(10L, "rex", "available"));

        assertThat(result).isEqualTo(new PetstorePet(10L, "rex", "available"));
    }

    @Test
    void createPet_throwsPetstoreUnavailableExceptionWhenPetstoreResponds500() {
        mockServer.expect(requestTo(BASE_URL + "/pet"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> petstoreClient.createPet(new PetstorePet(1L, "rex", "available")))
                .isInstanceOf(PetstoreUnavailableException.class);
    }
}
