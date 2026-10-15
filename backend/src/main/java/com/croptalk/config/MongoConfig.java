package com.croptalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Enables createdAt/updatedAt maintenance (Mongoose "timestamps: true" on the user schema). */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
