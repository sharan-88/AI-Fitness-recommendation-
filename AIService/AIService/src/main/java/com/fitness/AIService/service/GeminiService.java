package com.fitness.AIService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
@Service

public class GeminiService {

    private final WebClient webClient;

    @Value("${GEMINI_API_KEY}")
    private String geminiapikey;
    @Value("${GEMINI_API_URL}")
    private String geminiapiurl;

    public GeminiService(WebClient.Builder  webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
// request format to be sent
//    {
//        "contents": [{
//            "parts": [{
//                "text": prompt
//            }]
//        }]
//    }

    public String getAnswer(String prompt ){
        Map<String,Object> requestbody = Map.of(
                "contents",new Object[]{
                        Map.of("parts",new Object[]{
                             Map.of("text",prompt)
                        })
                }
//                ,"response_mime_type", "application/json"
        );

        String response = webClient.post()
                .uri(geminiapiurl+geminiapikey)
                .bodyValue(requestbody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;

    }


}
