package com.croptalk.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/** Port of config/dbconnect.js: connect at startup and exit(1) if MongoDB is unreachable. */
@Component
public class DbConnectionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DbConnectionRunner.class);

    private final MongoTemplate mongoTemplate;

    public DbConnectionRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            log.info("connected to mongodb..");
        } catch (Exception err) {
            log.error("Error: {}", err.getMessage());
            System.exit(1);
        }
    }
}
