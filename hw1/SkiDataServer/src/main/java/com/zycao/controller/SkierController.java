package com.zycao.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/skiers")
public class SkierController {

//    private static final AtomicInteger totalApiCalls = new AtomicInteger(0);

    @PostMapping("/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}")
    public ResponseEntity<String> handlePost(
            @PathVariable("resortID") Integer resortID,
            @PathVariable("seasonID") Integer seasonID,
            @PathVariable("dayID") Integer dayID,
            @PathVariable("skierID") Integer skierID)
            {


        // Validate input (resortID and skierID are automatically validated by Spring)
        if (seasonID < 1900 || seasonID > 2024 || dayID < 1 || dayID > 366) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid input");
        }
//
//        int currentCount = totalApiCalls.incrementAndGet();
//        System.out.println("Total number of API calls received: " + currentCount);
//        System.out.println("Success");

        return ResponseEntity.status(HttpStatus.CREATED).body("write successful");
    }
}
