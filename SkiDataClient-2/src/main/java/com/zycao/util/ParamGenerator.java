package com.zycao.util;

import com.zycao.model.SkierLiftRideEvent;

import java.util.Random;

public class ParamGenerator {
    private static final Random random = new Random();
//    private static final String baseUrl = "http://localhost:8080/skiers/";
    private static final String baseUrl = "http://35.91.85.112:8080/skiers/";

    public static SkierLiftRideEvent generateRandomEvent() {
        Integer skierID = 1 + random.nextInt(100000);
        Integer resortID = 1 + random.nextInt(10);
        Integer liftID = 1 + random.nextInt(40);
        Integer seasonID = 2024;
        Integer dayID = 1;
        Integer time = 1 + random.nextInt(360);

        return new SkierLiftRideEvent(skierID, resortID, liftID, seasonID, dayID, time);
    }

    public static String parseEvent(SkierLiftRideEvent event){
        return baseUrl + event.getResortID() +
                "/seasons/" + event.getSeasonID() +
                "/days/" + event.getDayID() +
                "/skiers/" + event.getSkierID();
    }
}
