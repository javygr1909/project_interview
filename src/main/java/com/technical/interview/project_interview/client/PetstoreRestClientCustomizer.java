package com.technical.interview.project_interview.client;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PetstoreRestClientCustomizer implements RestClientCustomizer {

    private final PetstoreProperties properties;

    public PetstoreRestClientCustomizer(PetstoreProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(RestClient.Builder restClientBuilder) {
        restClientBuilder.requestFactory(requestFactory());
    }

    private ClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
