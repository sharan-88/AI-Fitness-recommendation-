package com.fitness.AIService.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.AIService.model.Activity;
import com.fitness.AIService.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public  Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptforActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        Recommendation recommendation = processAiresponse(activity, aiResponse);
        log.info("Response from AI {}", aiResponse);
        return recommendation;

    }
    private Recommendation    processAiresponse(Activity activity,String aiResponse){
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(aiResponse);
            JsonNode textNode = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    ;
            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```","")
                    .trim();

            JsonNode analysisjson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisjson.path("analysis");
            StringBuilder fullanalysis = new StringBuilder();
            addanalysis(fullanalysis,analysisNode,"overall","overall:");
            addanalysis(fullanalysis,analysisNode,"pace","pace:");
            addanalysis(fullanalysis,analysisNode,"heartRate","heartRate:");
            addanalysis(fullanalysis,analysisNode,"caloriesBurned","caloriesBurned:");

            List<String> imporvements = extractImporvements(analysisjson.path("improvements"));
            List<String>suggestions= extractSuggestions(analysisjson.path("suggestions"));
            List<String>safety= extractsafety(analysisjson.path("safety"));
            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId()).
                    activityType(activity.getType()).
                    recommendation(fullanalysis.toString().trim())
                    .improvements(imporvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();




        } catch (Exception e) {


            return  createDefaultRecommendation(activity);
        }

    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("No detailed AI analysis available. Please provide clearer activity metrics or try again.")
                .improvements(Collections.emptyList())
                .suggestions(Collections.emptyList())
                .safety(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .build();
    }


    private List<String> extractsafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (!safetyNode.isEmpty()){
            safetyNode.forEach(item ->safety.add(item.asText()));

        }
        return safety.isEmpty()? Collections.singletonList("no safety found"):safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionNode.isArray()) {
            suggestionNode.forEach(sugg -> {
                String description = sugg.path("description").asText();
                String workout = sugg.path("Workout").asText();
                suggestions.add(String.format("%s %s", workout, description));

            });


        }
        return suggestions.isEmpty()?
                Collections.singletonList("suggestion is empty")
                : suggestions;

    }

    private List<String> extractImporvements(JsonNode improvementNode) {

//        create list of improvements
        List<String> improvements = new ArrayList<>();
                if(improvementNode.isArray()){
                    improvementNode.forEach(imp ->{
                        String area = imp.path("area").asText();
                        String recommendation= imp.path("recommendation").asText();
                        improvements.add(String.format("%s %s",area,recommendation));

                    });
                }
                return improvements.isEmpty()?
                        Collections.singletonList("no specific improvements found"):improvements;
    }

    private void addanalysis(StringBuilder fullanalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()){
            fullanalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");

        }
    }


    private String createPromptforActivity(Activity activity) {
        return String.format(
                """
                        Analyze the following fitness activity and provide a detailed response strictly in the exact JSON format shown below. Do not include any extra text, explanations, or commentary outside of the JSON structure.
                        
                              Return the response in this format only:
                        
                              {
                                "analysis": {
                                  "overall": "Overall performance summary",
                                  "pace": "Analysis of pace or movement intensity",
                                  "heartRate": "Heart rate performance analysis (if available, otherwise state 'Not Provided')",
                                  "caloriesBurned": "Calorie burn effectiveness and what it indicates"
                                },
                                "improvements": [
                                  {
                                    "area": "Specific area to improve",
                                    "recommendation": "Detailed, actionable improvement suggestion"
                                  }
                                ],
                                "suggestions": [
                                  {
                                    "Workout": "Workout name",
                                    "description": "Detailed reasoning and how it helps in progress"
                                  }
                                ],
                                "safety": [
                                  "Clear safety guideline 1",
                                  "Clear safety guideline 2"
                                ]
                              }
                        
                              Activity Details:
                              - Activity Type: %s
                              - Duration: %d minutes
                              - Calories Burned: %d
                              - Additional Metrics: %s
                        
                              Focus your analysis on:
                              1. Performance evaluation based on duration, activity type, and calories burned.
                              2. Practical improvement strategies the user can implement.
                              3. Specific workout suggestions for progression.
                              4. Safety considerations relevant to the intensity and type of activity.
                        
                              Ensure the response follows the exact JSON format shown above.
                        
                              Return the output as a raw JSON object.
                              
                        
                             
                        """, activity.getType(),
                            activity.getDuration(),
                            activity.getCaloriesBurned(),
                            activity.getAdditionalMetrics()
        );

    }


}

//
//Do NOT wrap the JSON in quotes.
//Do NOT escape the JSON.
//Do NOT include \n or \t escaped characters.
//Do NOT include markdown formatting.
//Do NOT include ```json or ``` fences.
//Return ONLY the JSON object itself, nothing else.
//
//