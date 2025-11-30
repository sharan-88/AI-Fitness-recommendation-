package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityContoller {
    @Autowired
    private ActivityService activityService;
    @GetMapping("/act")
    public String demo(){
        return "this is very important ";
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> trackAtivity(@RequestBody ActivityRequest request){
        System.out.println("🔥 Received startTime: " + request.getStartTime());
        return ResponseEntity.ok(activityService.trackAtivity(request));

    }
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivity(@RequestHeader("x-user-ID") String userId){
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId){
        return ResponseEntity.ok(activityService.getActivities(activityId));
    }



}
