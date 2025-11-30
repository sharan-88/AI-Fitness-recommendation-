package com.fitness.activityservice.service;

import com.fitness.activityservice.Repo.ActivityRepo;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ActivityService {
    @Autowired
    private  ActivityRepo repository;
    @Autowired
    private UserValidationService userValidationService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;



    public ActivityResponse trackAtivity(ActivityRequest request) {
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if (!isValidUser)
            throw new RuntimeException("Invalid user"+request.getUserId());
        Activity activity  = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .startTime(request.getStartTime())
                .caloriesBurned(request.getCaloriesBurned())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = repository.save(activity);
//        after saving activity to database publish it to rabbitmq for AI processing
        try{
            rabbitTemplate.convertAndSend(exchange,routingKey,savedActivity);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mapTOResponse(savedActivity);


    }
    private ActivityResponse mapTOResponse(Activity activity){
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setDuration(activity.getDuration());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedTime(activity.getCreatedTime());
        response.setUpdatedTime(activity.getUpdatedTime());
        return response;



    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities =  repository.findByUserId(userId);
        return activities.stream()
                .map(this::mapTOResponse)
                .toList();
    }

    public ActivityResponse getActivities(String activityId) {
        return repository.findById(activityId)
                .map(this::mapTOResponse)
                .orElseThrow(()-> new RuntimeException("activity not found "+ activityId));
    }
}
