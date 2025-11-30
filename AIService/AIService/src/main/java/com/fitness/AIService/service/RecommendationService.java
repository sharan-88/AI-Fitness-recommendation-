package com.fitness.AIService.service;

import com.fitness.AIService.model.Recommendation;
import com.fitness.AIService.repo.RecommendationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
//  Lombok automatically generates a constructor that
//  includes a parameter for every final field or any field
//  explicitly marked with @NonNull.
public class RecommendationService {

    private final RecommendationRepo recommendationRepo;


    public List<Recommendation> getUserRecommendation(String userId) {
        return recommendationRepo.findByUserId(userId);
    }

    public Recommendation gettActivityRecommendation(String activityId) {
        System.out.println("searching for "+activityId);
        return recommendationRepo.findByActivityId(activityId)
                .orElseThrow(()-> new RuntimeException(" no recommendation found for"+activityId ));
    }
}
