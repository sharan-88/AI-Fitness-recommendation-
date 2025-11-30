package com.fitness.activityservice.service;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@AllArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebclient;

    public boolean validateUser(String userId) {
        try {
            return userServiceWebclient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

        }catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("User not found: " + userId);
            }
        } catch (WebClientException e) {
            throw new RuntimeException("Error calling User Service: " + e.getMessage(), e);
        }


        return false;
    }
}
