package com.zycao.controller;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycao.entity.SkiDataPayload;
import com.zycao.service.RMQService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/skiers")
@Validated
public class SkierController {
    private static final Logger logger = LoggerFactory.getLogger(SkierController.class);

    private static final AtomicInteger totalApiCalls = new AtomicInteger(0);

    @Autowired
    private RMQService rmqService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping("/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}")
    public ResponseEntity<String> doPost(
            @PathVariable("resortID") @Min(value = Integer.MIN_VALUE) @Max(value = Integer.MAX_VALUE) Integer resortID,
            @PathVariable("seasonID") @Min(2000) @Max(2024) Integer seasonID,
            @PathVariable("dayID") @Min(1) @Max(366) Integer dayID,
            @PathVariable("skierID") @Min(value = Integer.MIN_VALUE) @Max(value = Integer.MAX_VALUE) Integer skierID,
            @Validated @RequestBody SkierRequest skierRequest) throws JsonProcessingException {

        SkiDataPayload payload = new SkiDataPayload(resortID, seasonID, dayID, skierID, skierRequest.time, skierRequest.liftID);
        String jsonPayload = objectMapper.writeValueAsString(payload);
        rmqService.enqueueMessage(jsonPayload);
        int currentCount = totalApiCalls.incrementAndGet();
        logger.info("Total number of API calls received: " + currentCount);
        return ResponseEntity.status(HttpStatus.CREATED).body("Request Processed");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Input");
    }

    @Data
    public static class SkierRequest {
        @NotNull
        @Min(value = Integer.MIN_VALUE)
        @Max(value = Integer.MAX_VALUE)
        private Integer time;

        @NotNull
        @Min(value = Integer.MIN_VALUE)
        @Max(value = Integer.MAX_VALUE)
        private Integer liftID;
    }

}
