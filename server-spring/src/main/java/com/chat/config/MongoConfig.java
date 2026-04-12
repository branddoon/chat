package com.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables MongoDB auditing support so that {@code @CreatedDate} and
 * {@code @LastModifiedDate} fields on documents are automatically populated
 * on insert and update operations.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
