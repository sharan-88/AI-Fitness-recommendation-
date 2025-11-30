package com.fitness.userservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebclientConfig {
    @Bean
    @LoadBalanced
//    When you use WebClient in a Spring Cloud + Eureka environment,
//    you might want to call another microservice by its service name
//    (like http://activity-service) — not by hardcoded IP and port.
    public WebClient.Builder webclientBuilder(){
        return WebClient.builder();
    }

    public WebClient userServiceWebClient(WebClient.Builder webclientBuilder){
        return webclientBuilder
                .baseUrl("http://USER-SERVICE")
                .build();
    }


}
