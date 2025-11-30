package com.fitness.AIService.model;



import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.Map;

@Data

@Document(collection="activities")
public class Activity {

    private String id;
    private String userId;
    private String Type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;

    private Map<String,Object> additionalMetrics;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}

