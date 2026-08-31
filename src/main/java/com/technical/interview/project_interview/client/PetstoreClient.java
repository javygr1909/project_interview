package com.technical.interview.project_interview.client;

public interface PetstoreClient {

    PetstorePet getPet(Long petId);

    PetstorePet createPet(PetstorePet pet);
}
