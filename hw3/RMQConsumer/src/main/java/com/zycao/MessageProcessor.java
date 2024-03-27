package com.zycao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycao.entity.SkiDataPayload;
import redis.clients.jedis.Jedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int BATCH_SIZE = 500; // default batch size
    private static List<SkiDataPayload> batch = new ArrayList<>();
    private static Jedis jedis = new Jedis("localhost", 6379);

    /**
     * Accumulate consumed message and process in batches
     * @param message
     */
    public static void processMessage(String message) {
        try {
            // Deserialize the message back into SkiDataPayload object
            SkiDataPayload payload = objectMapper.readValue(message, SkiDataPayload.class);
            batch.add(payload);
            if (batch.size() >= BATCH_SIZE) {
                processBatch(batch);
                batch.clear(); // process and clear the batch once upon reaching limit
            }
        } catch (Exception e) {
            logger.error("Error processing message: ", e);
        }
    }

    /**
     * using pipeline to store batch of data into redis
     * @param batch
     */
    private static void processBatch(List<SkiDataPayload> batch) {
        Pipeline pipeline = jedis.pipelined();
        for (SkiDataPayload payload: batch){
            String daysSkiedKey = String.format("skier:%d:seasons:%d:days", payload.getSkierID(), payload.getSeasonID());
            String verticalTotalsKey = String.format("skier:%d:seasons:%d:days:%d:vertical", payload.getSkierID(), payload.getSeasonID(), payload.getDayID());
            String liftsRiddenKey = String.format("skier:%d:seasons:%d:days:%d:lifts", payload.getSkierID(), payload.getSeasonID(), payload.getDayID());
            String resortSkierKey = String.format("resort:%d:day:%d:skiers", payload.getResortID(), payload.getDayID());

            pipeline.sadd(daysSkiedKey, payload.getDayID().toString()); // Storing days skied by a skier
            int verticalIncrease = payload.getLiftID() * 10;
            pipeline.incrBy(verticalTotalsKey, verticalIncrease); // Updating vertical totals for a ski day
            pipeline.sadd(liftsRiddenKey, payload.getLiftID().toString()); // Recording a lift ride
            pipeline.sadd(resortSkierKey, payload.getSkierID().toString());  // Recording a skier for a given resort
            //logger.info("Updated a message to Redis: {}", payload);
        }
        pipeline.sync();

    }
}
