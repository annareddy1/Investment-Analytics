package com.marketlens.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * ✅ Custom Health Indicator for MongoDB
 * - Checks MongoDB connectivity
 * - Executes ping command to verify database is responsive
 * - Reports database name and server info
 */
@Component("mongodb")
@RequiredArgsConstructor
@Slf4j
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            // Execute ping command to check MongoDB connectivity
            Document pingResult = mongoTemplate.getDb()
                    .runCommand(new Document("ping", 1));

            if (pingResult.getDouble("ok") == 1.0) {
                String databaseName = mongoTemplate.getDb().getName();
                
                return Health.up()
                        .withDetail("database", "MongoDB")
                        .withDetail("status", "Connected")
                        .withDetail("databaseName", databaseName)
                        .withDetail("pingCommand", "ok")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "MongoDB")
                        .withDetail("error", "Ping command failed")
                        .build();
            }

        } catch (Exception e) {
            log.error("MongoDB health check failed", e);
            return Health.down()
                    .withDetail("database", "MongoDB")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
