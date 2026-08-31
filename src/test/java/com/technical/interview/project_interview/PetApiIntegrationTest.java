package com.technical.interview.project_interview;

import com.sun.net.httpserver.HttpServer;
import com.technical.interview.project_interview.dto.CreatePetResponse;
import com.technical.interview.project_interview.dto.GetPetResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The project's only integration test: boots the real Spring context (Tomcat included)
 * and confirms that Controller, Service, PetstoreClientImpl, PetstoreProperties and
 * PetstoreRestClientCustomizer are correctly wired together by the container.
 * Does not repeat the error scenarios already covered by the unit tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PetApiIntegrationTest {

    private static HttpServer petstoreStub;

    @DynamicPropertySource
    static void petstoreProperties(DynamicPropertyRegistry registry) throws Exception {
        petstoreStub = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        petstoreStub.createContext("/pet/555", exchange -> {
            byte[] body = "{\"id\":555,\"name\":\"rex\",\"status\":\"available\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        petstoreStub.createContext("/pet", exchange -> {
            byte[] body = "{\"id\":20,\"name\":\"milo\",\"status\":\"pending\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        petstoreStub.start();

        registry.add("petstore.base-url", () -> "http://localhost:" + petstoreStub.getAddress().getPort());
    }

    @AfterAll
    static void stopPetstoreStub() {
        petstoreStub.stop(0);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getPet_endToEndWithTheRealSpringContext() {
        ResponseEntity<GetPetResponse> response = restTemplate.getForEntity("/api/pet/555", GetPetResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new GetPetResponse(555L, "rex", "available"));
    }

    @Test
    void createPet_endToEndWithTheRealSpringContext() {
        RequestEntity<String> request = RequestEntity.post(URI.create("/api/pet"))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"id\":20,\"name\":\"milo\",\"status\":\"pending\"}");

        ResponseEntity<CreatePetResponse> response = restTemplate.exchange(request, CreatePetResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("milo");
        assertThat(response.getBody().status()).isEqualTo("pending");
        assertThat(response.getBody().transactionId()).isNotNull();
        assertThat(response.getBody().dateCreated()).isNotNull();
    }
}
