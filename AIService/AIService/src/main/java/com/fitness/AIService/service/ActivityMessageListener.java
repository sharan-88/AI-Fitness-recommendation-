package com.fitness.AIService.service;

import com.fitness.AIService.model.Activity;
import com.fitness.AIService.model.Recommendation;
import com.fitness.AIService.repo.RecommendationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepo recommendationRepo;


    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("received activity for processing {}",activity.getId());

        Recommendation recommendation = aiService.generateRecommendation(activity);
        log.info("generated recommendation: {}", recommendation);
        Recommendation saved = recommendationRepo.save(recommendation);
        log.info("-------------Recommendation saved with ID---------------------: {}", saved.getId());
    }
}
